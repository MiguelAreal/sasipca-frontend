package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.*
import sasipca.storage.ApiConfig
import sasipca.storage.requestWithAuth

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

    // --- ENTREGAS (Histórico) ---
    // Nota: O teu DeliveryRepository já tinha getDeliveries, mas este endpoint
    // usa a View VDelivery que é mais leve para listagens grandes.
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