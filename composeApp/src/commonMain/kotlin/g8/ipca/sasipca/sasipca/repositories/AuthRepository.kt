package g8.ipca.sasipca.sasipca.repositories

import g8.ipca.sasipca.sasipca.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepository(private val client: HttpClient) {

    suspend fun login(email: String, password: String): Result<LoginSuccessResponse> {
        return try {
            val response = client.post("https://192.168.1.17/api/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            return when (response.status) {
                HttpStatusCode.OK -> {
                    val successResponse: LoginSuccessResponse = response.body()
                    Result.success(successResponse)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                    val errorResponse: LoginErrorResponse = response.body()
                    Result.failure(Exception(errorResponse.message))
                }
                else -> Result.failure(Exception("Unexpected error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
