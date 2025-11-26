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
        val idToken = msAuthManager.signIn()
            ?: return Result.failure(Exception("Login Microsoft cancelado ou falhou."))

        // 2. Enviar para o teu backend
        return try {
            val response = client.post("${ApiConfig.baseUrl()}/auth/login/microsoft") {
                contentType(ContentType.Application.Json)
                // Criar um DTO simples para enviar o token
                setBody(mapOf("idToken" to idToken))
            }

            when (response.status) {
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
                    Result.failure(Exception("Erro no backend: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Faz logout
     */
    suspend fun logout(): Result<Resposta> {
        return try {
            val resposta: Resposta = client.requestWithAuth(
                method = HttpMethod.Post,
                url = URLBuilder(ApiConfig.baseUrl()).apply {
                    appendPathSegments("auth", "logout")
                }.buildString()
            )

            // Limpa sessão após sucesso
            SessionManager.clear()
            Result.success(resposta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
