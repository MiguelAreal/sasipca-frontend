package sasipca.repositories

import sasipca.storage.ApiConfig
import sasipca.storage.SessionManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.storage.requestWithAuth
import sasipca.models.*
import sasipca.storage.markAsRefreshTokenRequest

class AuthRepository(private val client: HttpClient) {

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

    /**
     * Faz login
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("${ApiConfig.baseUrl()}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            return when (response.status) {
                HttpStatusCode.OK -> {
                    val successResponse: AuthResponse = response.body()
                    // Armazena sessão

                    SessionManager.saveSession(
                        token = successResponse.accessToken,
                        refreshToken = successResponse.refreshToken,
                        userID = successResponse.userID,
                        userName = successResponse.userName
                    )
                    Result.success(successResponse)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                    val errorResposta: Resposta = response.body()
                    Result.failure(Exception(errorResposta.message))
                }
                else -> Result.failure(Exception("Unexpected error: ${response.status.value}"))
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
                url = "${ApiConfig.baseUrl()}/auth/logout"
            )

            // Limpa sessão após sucesso
            SessionManager.clear()
            Result.success(resposta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
