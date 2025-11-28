package sasipca.repositories

import sasipca.storage.ApiConfig
import sasipca.storage.SessionManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.auth.MicrosoftAuthManager
import sasipca.storage.requestWithAuth
import sasipca.models.*
import sasipca.storage.markAsRefreshTokenRequest

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
                    userName = successResponse.userName
                )
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
        // Se isto falhar ou for cancelado, retorna logo (nada a limpar)
        val idToken = msAuthManager.signIn()
            ?: return Result.failure(Exception("Login Microsoft cancelado ou falhou."))

        // 2. Tentar enviar para o Backend
        try {
            val response = client.post("${ApiConfig.baseUrl()}/auth/login/microsoft") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idToken" to idToken))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    // SUCESSO TOTAL
                    val successResponse: AuthResponse = response.body()
                    SessionManager.saveSession(
                        token = successResponse.accessToken,
                        refreshToken = successResponse.refreshToken,
                        userID = successResponse.userID,
                        userName = successResponse.userName
                    )
                    return Result.success(successResponse)
                }

                // ERROS DO BACKEND (401, 400, 500, etc.)
                else -> {
                    // O backend rejeitou (ex: email pessoal, servidor em baixo, erro 500)
                    // MAS a Microsoft já deu o "OK" localmente.
                    // TEMOS DE LIMPAR A SESSÃO MICROSOFT IMEDIATAMENTE.
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
            // ERROS DE REDE OU CRASH (Sem internet, Timeout)
            // Também aqui temos de limpar a sessão Microsoft, senão o utilizador
            // fica preso no estado "logado" sem conseguir tentar de novo.
            silentSignOut()
            return Result.failure(e)
        }
    }

    /**
     * Helper para limpar sessão sem bloquear ou lançar excepções para a UI
     */
    private suspend fun silentSignOut() {
        try {
            msAuthManager.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            // Ignoramos erro no logout porque já estamos num fluxo de erro
        }
    }

    /**
     * Faz logout completo (Backend + Microsoft + Sessão Local)
     */
    suspend fun logout(): Result<Resposta> {
        var backendResult: Result<Resposta>

        // Avisar o Backend
        try {
            val resposta: Resposta = client.requestWithAuth(
                method = HttpMethod.Post,
                url = URLBuilder(ApiConfig.baseUrl()).apply {
                    appendPathSegments("auth", "logout")
                }.buildString()
            )
            backendResult = Result.success(resposta)
        } catch (e: Exception) {
            // Se falhar (ex: sem net), não faz mal. O importante é limpar localmente.
            backendResult = Result.failure(e)
        }

        // Limpar dados da Microsoft (MSAL)
        // Isto impede o bug de "login loop" ou "account already signed in"
        try {
            msAuthManager.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Limpar Sessão Interna da App
        SessionManager.clear()

        return backendResult
    }

}
