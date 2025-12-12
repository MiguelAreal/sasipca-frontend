package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.*
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

class HistoryRepository(private val client: HttpClient) {

    // --- MOVIMENTOS ---
    suspend fun getMovements(): List<MovementHistory> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("movements")
            }.buildString()
        )
    }

    suspend fun getMovementDetails(id: Int): MovementDetail {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("movements", id.toString())
            }.buildString()
        )
    }

    // Busca o histórico filtrado por produto
    suspend fun getProductHistory(barcode: String): List<MovementHistory> {
        return try {
            client.requestWithAuth<List<MovementHistory>>(
                method = HttpMethod.Get,
                url = "${ApiConfig.baseUrl()}/products/$barcode/history"
            )
        } catch (e: Exception) {
            println("Erro ao carregar histórico do produto: ${e.message}")
            emptyList()
        }
    }

    // --- ENTREGAS (Histórico) ---
    suspend fun getDeliveriesHistory(dateFrom: String? = null, dateTo: String? = null): List<DeliveryHistory> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("deliveries")
                if (dateFrom != null) parameters.append("DateFrom", dateFrom)
                if (dateTo != null) parameters.append("DateTo", dateTo)
            }.buildString()
        )
    }

    suspend fun getDeliveryDetails(id: Int): DeliveryDetail {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("deliveries", id.toString())
            }.buildString()
        )
    }
}