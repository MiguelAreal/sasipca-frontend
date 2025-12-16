package sasipca.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.PartData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sasipca.storage.SessionManager

// Mutex para impedir refresh simultâneo (Race Condition)
//
val refreshMutex = Mutex()

/**
 * Faz uma requisição com autenticação JWT e tenta renovar o token automaticamente.
 */
suspend inline fun <reified T> HttpClient.requestWithAuth(
    method: HttpMethod,
    url: String,
    body: Any? = null,
    formData: List<PartData>? = null
): T {
    val currentToken = SessionManager.getAccessToken() ?: throw Exception("Token ausente")

    var response: HttpResponse
    try {
        response = executeRequest(this, method, url, currentToken, body, formData)
    } catch (e: ClientRequestException) {
        response = e.response
    }

    // 1. Se der 401, entra na lógica de Refresh Sincronizada
    if (response.status == HttpStatusCode.Unauthorized) {

        val newToken = refreshMutex.withLock {
            val mostRecentToken = SessionManager.getAccessToken()

            // Double-check: Se o token mudou enquanto esperávamos, usamos o novo
            if (mostRecentToken != null && mostRecentToken != currentToken) {
                mostRecentToken
            } else {
                // Faz o refresh real
                val refreshResult = ApiClient.refreshToken()

                if (refreshResult.isSuccess) {
                    val authResponse = refreshResult.getOrThrow()
                    SessionManager.saveSession(
                        token = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        userID = SessionManager.getUserId() ?: 0,
                        userName = SessionManager.getUserName() ?: "",
                        role = SessionManager.getUserRole() ?: ""
                    )
                    authResponse.accessToken
                } else {
                    SessionManager.triggerLogout()
                    throw SasipcaApiException("Sessão expirada. Por favor faça login novamente.")
                }
            }
        }

        // 2. Tenta novamente (Retry) com o token novo
        val retryResponse = executeRequest(this, method, url, newToken, body, formData)

        if (retryResponse.status.isSuccess()) {
            return retryResponse.body()
        } else {
            val errorMsg = parseErrorBody(retryResponse)
            throw SasipcaApiException(errorMsg)
        }
    }

    // 3. Sucesso do primeiro pedido
    if (response.status.isSuccess()) {
        return response.body()
    } else {
        val errorMsg = parseErrorBody(response)
        throw SasipcaApiException(errorMsg)
    }
}

// Função auxiliar (não inline) para executar o HTTP
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

// Função auxiliar para ler erros e evitar erros de inline
suspend fun parseErrorBody(response: HttpResponse): String {
    return try {
        // Tenta ler como JSON do nosso modelo
        val errorObj = response.body<ApiErrorResponse>()
        errorObj.message ?: errorObj.error ?: "Erro desconhecido (${response.status})"
    } catch (e: Exception) {
        try {
            // Se falhar o JSON, lê como texto simples
            response.bodyAsText()
        } catch (e2: Exception) {
            "Erro de conexão: ${response.status}"
        }
    }
}