package sasipca.repositories

import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import sasipca.models.ReceiptPost
import sasipca.models.Resposta
import sasipca.storage.ApiConfig
import sasipca.storage.requestWithAuth

class ReceiptRepository(private val client: HttpClient) {
    /**
     * Executa receção de stock
     */
    suspend fun postReceipt(body: ReceiptPost): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stock","receipts")
            }.buildString(),
            body = body
        )
    }
}