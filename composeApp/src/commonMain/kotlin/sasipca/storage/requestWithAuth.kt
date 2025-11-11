package sasipca.storage

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.ApiClient

/**
 * Faz uma requisição com autenticação JWT e tenta renovar o token automaticamente se expirar.
 */
suspend inline fun <reified T> HttpClient.requestWithAuth(
    method: HttpMethod,
    url: String,
    body: Any? = null
): T {
    // 🚫 Não aplicar refresh automático se for o próprio pedido de refresh
    val isRefreshRequest = attributes.contains(RefreshTokenRequestKey)
    if (isRefreshRequest) {
        val response = request(url) {
            this.method = method
            header(HttpHeaders.Authorization, "Bearer ${SessionManager.getAccessToken()}")
            if (body != null) setBody(body)
        }
        return response.body()
    }

    val token = SessionManager.getAccessToken() ?: throw Exception("Token ausente")

    var response: HttpResponse
    try {
        response = executeRequest(this, method, url, token, body)
    } catch (e: ClientRequestException) {
        response = e.response
    }

    // Se o primeiro request deu 401 (token expirado)
    if (response.status == HttpStatusCode.Unauthorized) {
        val www = response.headers["WWW-Authenticate"]
        if (www?.contains("expired_token") == true) {
            // 🔄 tenta refresh
            val refreshResult = ApiClient.refreshToken()
            if (refreshResult.isSuccess) {
                val newToken = refreshResult.getOrThrow().accessToken
                SessionManager.setAccessToken(newToken)

                // 🔁 repetir o request original com novo token
                val retryResponse = executeRequest(this, method, url, newToken, body)
                if (retryResponse.status.isSuccess()) {
                    return retryResponse.body()
                } else {
                    throw ClientRequestException(retryResponse, "Retry failed: ${retryResponse.status}")
                }
            } else {
                SessionManager.clear()
                throw Exception("Não foi possível renovar o token.")
            }
        }
    }

    if (response.status.isSuccess()) {
        return response.body()
    } else {
        throw ClientRequestException(response, "Request falhou com status ${response.status}")
    }
}

/**
 * Função auxiliar (fora da inline) que executa o request com o token fornecido.
 */
suspend fun executeRequest(
    client: HttpClient,
    method: HttpMethod,
    url: String,
    token: String,
    body: Any? = null
): HttpResponse {
    return client.request(url) {
        this.method = method
        header(HttpHeaders.Authorization, "Bearer $token")
        if (body != null) setBody(body)
    }
}
