package sasipca.repositories

import sasipca.network.ApiConfig
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.auth.MicrosoftAuthManager
import sasipca.network.requestWithAuth
import sasipca.models.*
import sasipca.network.markAsRefreshTokenRequest

class AuthRepository(
    private val client: HttpClient,
    private val msAuthManager: MicrosoftAuthManager
) {

    /**
     * Renova o access token usando o refresh token
     */
    suspend fun refreshToken(): Result<AuthResponse> {
        val response: HttpResponse
        try {
            val expiredAccessToken = SessionManager.getAccessToken()
                ?: return Result.failure(Exception("Access token ausente"))

            response = client.post("${ApiConfig.baseUrl()}/auth/refresh") {
                markAsRefreshTokenRequest()
                header(HttpHeaders.Cookie, "refreshToken=${SessionManager.getRefreshToken()}")
                header(HttpHeaders.Authorization, "Bearer $expiredAccessToken")
                contentType(ContentType.Application.Json)
            }
        } catch (e: Exception) {
            SessionManager.clear()
            return Result.failure(e)
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val successResponse: AuthResponse = response.body()
                SessionManager.saveSession(
                    token = successResponse.accessToken,
                    refreshToken = successResponse.refreshToken,
                    userID = successResponse.userID,
                    userName = successResponse.userName,
                    role = successResponse.role
                )
                // Opcional: Podes querer renovar o token FCM no refresh também,
                // mas geralmente só no login é suficiente.
                Result.success(successResponse)
            }
            else -> {
                SessionManager.clear()
                val errorMessage = try {
                    response.body<Resposta>().message
                } catch (e: Exception) {
                    "Failed to refresh token: ${response.status}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }


    suspend fun loginMicrosoft(): Result<AuthResponse> {
        // 1. Obter IdToken da Microsoft
        val idToken = msAuthManager.signIn()
            ?: return Result.failure(Exception("Login Microsoft cancelado ou falhou."))

        // 2. Tentar enviar para o Backend
        try {
            val response = client.post("${ApiConfig.baseUrl()}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idToken" to idToken))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    // SUCESSO TOTAL
                    val successResponse: AuthResponse = response.body()

                    // 1. Guarda a sessão (Tokens de acesso)
                    SessionManager.saveSession(
                        token = successResponse.accessToken,
                        refreshToken = successResponse.refreshToken,
                        userID = successResponse.userID,
                        userName = successResponse.userName,
                        role = successResponse.role
                    )

                    // 2. [NOVO] Envia o Token FCM pendente para o backend
                    // Fazemos isto AQUI porque agora temos a certeza que temos um token de acesso válido
                    sendFcmTokenSafely()

                    return Result.success(successResponse)
                }

                // ERROS DO BACKEND
                else -> {
                    silentSignOut()
                    val msg = try {
                        response.body<Resposta>().message
                    } catch (e: Exception) {
                        "Erro no backend: ${response.status}"
                    }
                    return Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            silentSignOut()
            return Result.failure(e)
        }
    }

    /**
     * Tenta enviar o token FCM guardado no SettingsManager para a API.
     * Envolto em try-catch para não falhar o Login se a notificação falhar.
     */
    private suspend fun sendFcmTokenSafely() {
        val fcmToken = SettingsManager.getFcmToken() ?: return

        try {
            // Usamos o SessionManager para pegar o token que acabámos de guardar
            val accessToken = SessionManager.getAccessToken() ?: return

            client.post("${ApiConfig.baseUrl()}/notifications/device") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                // Usa o DTO que definiste no NotificationRepository ou cria um mapa simples
                setBody(DeviceTokenDto(fcmToken))
            }
            println("AuthRepo: Token FCM enviado com sucesso após login.")
        } catch (e: Exception) {
            // Apenas logamos o erro, não queremos estragar a experiência de login do user
            println("AuthRepo: Aviso - Falha ao registar dispositivo FCM: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun silentSignOut() {
        try {
            msAuthManager.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun logout(): Result<Resposta> {
        var backendResult: Result<Resposta>

        try {
            val resposta: Resposta = client.requestWithAuth(
                method = HttpMethod.Post,
                url = URLBuilder(ApiConfig.baseUrl()).apply {
                    appendPathSegments("auth", "logout")
                }.buildString()
            )
            backendResult = Result.success(resposta)
        } catch (e: Exception) {
            backendResult = Result.failure(e)
        }

        try {
            msAuthManager.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        SessionManager.clear()

        return backendResult
    }
}