package sasipca.network

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.PartData
import sasipca.storage.SessionManager

/**
 * Faz uma requisição com autenticação JWT e tenta renovar o token automaticamente.
 * Suporta JSON (via body) ou Multipart (via formData).
 */
suspend inline fun <reified T> HttpClient.requestWithAuth(
    method: HttpMethod,
    url: String,
    body: Any? = null,
    formData: List<PartData>? = null
): T {
    // Evita loop infinito se for o próprio request de refresh
    val isRefreshRequest = attributes.contains(RefreshTokenRequestKey)

    if (isRefreshRequest) {
        val response = request(url) {
            this.method = method
            header(HttpHeaders.Authorization, "Bearer ${SessionManager.getAccessToken()}")
            if (body != null) {
                setBody(body)
                contentType(ContentType.Application.Json)
            } else if (formData != null) {
                setBody(MultiPartFormDataContent(formData))
            }
        }
        return response.body()
    }

    val token = SessionManager.getAccessToken() ?: throw Exception("Token ausente")

    var response: HttpResponse
    try {
        response = executeRequest(this, method, url, token, body, formData)
    } catch (e: ClientRequestException) {
        response = e.response
    }

    // Se der 401 (Unauthorized)
    if (response.status == HttpStatusCode.Unauthorized) {

        // Tenta refresh
        val refreshResult = ApiClient.refreshToken() // Certifica-te que este método existe no teu ApiClient

        if (refreshResult.isSuccess) {
            val newToken = refreshResult.getOrThrow().accessToken
            SessionManager.setAccessToken(newToken)

            // Repetir request com novo token
            val retryResponse = executeRequest(this, method, url, newToken, body, formData)
            if (retryResponse.status.isSuccess()) {
                return retryResponse.body()
            } else {
                throw ClientRequestException(retryResponse, "Retry failed: ${retryResponse.status}")
            }
        } else {
            // Falha no refresh: A sessão expirou definitivamente.
            SessionManager.triggerLogout()
            throw Exception("Sessão expirada. Por favor faça login novamente.")
        }
    }

    if (response.status.isSuccess()) {
        return response.body()
    } else {
        val errorBody = try { response.body<String>() } catch (_: Exception) { "Erro desconhecido" }
        throw Exception("Request falhou (${response.status}): $errorBody")
    }
}

/**
 * Função auxiliar que executa o request.
 */
suspend fun executeRequest(
    client: HttpClient,
    method: HttpMethod,
    url: String,
    token: String,
    body: Any?,
    formData: List<PartData>?
): HttpResponse {
    return client.request(url) {
        this.method = method
        header(HttpHeaders.Authorization, "Bearer $token")

        if (formData != null) {
            setBody(MultiPartFormDataContent(formData))
        } else if (body != null) {
            setBody(body)
            contentType(ContentType.Application.Json)
        }
    }
}