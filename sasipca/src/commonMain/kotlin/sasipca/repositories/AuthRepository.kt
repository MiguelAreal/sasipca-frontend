package sasipca.repositories

import sasipca.network.ApiConfig
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.models.auth.MicrosoftAuthManager
import sasipca.network.requestWithAuth
import sasipca.models.*
import sasipca.network.markAsRefreshTokenRequest
import sasipca.utils.updateWidgets // <--- Importante

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
            updateWidgets()
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

                updateWidgets()

                Result.success(successResponse)
            }
            else -> {
                SessionManager.clear()
                updateWidgets()

                val errorMessage = try {
                    response.body<Resposta>().message
                } catch (_: Exception) {
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

                    // 1. Guarda a sessão (‘Tokens’ de acesso)
                    SessionManager.saveSession(
                        token = successResponse.accessToken,
                        refreshToken = successResponse.refreshToken,
                        userID = successResponse.userID,
                        userName = successResponse.userName,
                        role = successResponse.role
                    )

                    // 2. Envia o Token FCM pendente para o backend
                    sendFcmTokenSafely()

                    // 3. Atualiza o Widget (agora que sabemos o role e temos token)
                    updateWidgets()

                    return Result.success(successResponse)
                }

                // ERROS DO BACKEND
                else -> {
                    silentSignOut()
                    val msg = try {
                        response.body<Resposta>().message
                    } catch (_: Exception) {
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
     */
    private suspend fun sendFcmTokenSafely() {
        val fcmToken = SettingsManager.getFcmToken() ?: return

        try {
            val accessToken = SessionManager.getAccessToken() ?: return

            client.post("${ApiConfig.baseUrl()}/notifications/device") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(DeviceTokenDto(fcmToken))
            }
            println("AuthRepo: Token FCM enviado com sucesso após login.")
        } catch (e: Exception) {
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

        updateWidgets()

        return backendResult
    }
}