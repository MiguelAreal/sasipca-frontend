package sasipca.repositories

import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
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
            url = "${ApiConfig.baseUrl()}/stock/receipts",
            body = body
        )
    }
}