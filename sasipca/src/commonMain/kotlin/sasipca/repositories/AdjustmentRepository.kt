package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.Resposta
import sasipca.models.StockAdjustment
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

class AdjustmentRepository(private val client: HttpClient) {

    suspend fun adjustStock(dto: StockAdjustment): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Patch, // Backend usa HTTP PATCH
            url = "${ApiConfig.baseUrl()}/stock/adjusts",
            body = dto
        )
    }
}