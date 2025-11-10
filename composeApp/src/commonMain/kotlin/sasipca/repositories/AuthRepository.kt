package sasipca.repositories

import sasipca.storage.ApiConfig
import sasipca.storage.SessionManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.models.*
import sasipca.storage.markAsRefreshTokenRequest

class AuthRepository(private val client: HttpClient) {

    /**
     * Renova o access token usando o refresh token
     */
    suspend fun refreshToken(): Result<AuthResponse> {

        // ⬇️ ESTA É A LÓGICA CORRIGIDA (PROBLEMA 2) ⬇️
        val response: HttpResponse
        try {
            response = client.post("${ApiConfig.baseUrl()}/refresh") {

                markAsRefreshTokenRequest()
                header(HttpHeaders.Authorization, "Bearer ${SessionManager.getAccessToken()}")
                header(HttpHeaders.Cookie, "refreshToken=${SessionManager.getRefreshToken()}")
                contentType(ContentType.Application.Json)
            }
        } catch (e: Exception) {
            // Falha de rede, sem internet, etc.
            SessionManager.clear()
            return Result.failure(e)
        }

        // Agora verificamos o status ANTES de tentar ler o .body()
        return when (response.status) {
            HttpStatusCode.OK -> {
                // Sucesso, o corpo é AuthResponse
                val successResponse: AuthResponse = response.body()
                // Atualiza session local
                SessionManager.saveSession(
                    token = successResponse.accessToken,
                    refreshToken = successResponse.refreshToken,
                    userID = successResponse.userID,
                    userName = successResponse.userName,
                    expiresIn = successResponse.expiresIn
                )
                Result.success(successResponse)
            }
            else -> {
                // Falha (401, 400, etc), o refresh token é inválido.
                // O corpo é um objeto de Erro (ex: Resposta)
                SessionManager.clear()
                val errorMessage = try {
                    response.body<Resposta>().message
                } catch (e: Exception) {
                    "Falha ao renovar token: ${response.status}"
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
            val response = client.post("${ApiConfig.baseUrl()}/login") {
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
                        userName = successResponse.userName,
                        expiresIn = successResponse.expiresIn
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
            val resposta: Resposta = client.post("${ApiConfig.baseUrl()}/logout").body()

            // Limpa sessão após sucesso
            SessionManager.clear()
            Result.success(resposta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
