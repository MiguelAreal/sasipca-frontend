package g8.ipca.sasipca.sasipca.repositories

import g8.ipca.sasipca.sasipca.models.StockItemDTO
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.storage.SessionManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class StockRepository(private val client: HttpClient = ApiClient.client) {

    private val baseUrl = "https://192.168.1.17/api/products"

    suspend fun getStock(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "asc"
    ): List<StockItemDTO> {
        val token = SessionManager.getAuthToken() ?: throw Exception("Token não disponível")

        val response: HttpResponse = client.get(baseUrl) {
            header("Authorization", "Bearer $token")
            parameter("searchTerm", search)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Erro ao buscar stock: ${response.status}")
        }

        return response.body() // retornará List<StockItemDTO>
    }
}
