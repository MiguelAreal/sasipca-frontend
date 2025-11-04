package g8.ipca.sasipca.sasipca.repositories

import g8.ipca.sasipca.sasipca.models.*
import g8.ipca.sasipca.sasipca.storage.ApiConfig
import g8.ipca.sasipca.sasipca.storage.SessionManager
import g8.ipca.sasipca.sasipca.storage.authorizedRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepository(private val client: HttpClient) {
    suspend fun refreshToken(): Result<AuthResponse> {
        return try {
            // Faz a request autorizada
            val authResponse: AuthResponse = client.authorizedRequest<AuthResponse>(
                url = "${ApiConfig.baseUrl()}/refresh",
                method = HttpMethod.Post
            )

            // Guarda sessão localmente
            SessionManager.saveSession(
                token = authResponse.accessToken,
                refreshToken = authResponse.refreshToken,
                userID = authResponse.userID,
                userName = authResponse.userName,
                expiresIn = authResponse.expiresIn
            )

            Result.success(authResponse)

        } catch (e: Exception) {
            // Pode ser HttpResponseException, TimeoutException, etc
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("${ApiConfig.baseUrl()}/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            return when (response.status) {
                HttpStatusCode.OK -> {
                    val successResponse: AuthResponse = response.body()

                    // guardar sessão localmente
                    SessionManager.saveSession(
                        token = successResponse.accessToken,
                        refreshToken = successResponse.refreshToken,
                        userID = successResponse.userID,
                        userName = successResponse.userName,
                        expiresIn = successResponse.expiresIn
                    )
                    Result.success(successResponse)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                    val errorResponse: Response = response.body()
                    Result.failure(Exception(errorResponse.message))
                }
                else -> Result.failure(Exception("Unexpected error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Response> {
        return try {
            // Faz a request autorizada
            val response: Response = client.authorizedRequest(
                url = "${ApiConfig.baseUrl()}/logout",
                method = HttpMethod.Post
            )

            // Limpa sessão apenas após sucesso
            SessionManager.clear()
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

