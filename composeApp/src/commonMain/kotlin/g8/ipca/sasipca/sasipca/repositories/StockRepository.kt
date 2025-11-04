package g8.ipca.sasipca.sasipca.repositories

import g8.ipca.sasipca.sasipca.models.StockItemDTO
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.storage.ApiConfig
import g8.ipca.sasipca.sasipca.storage.SessionManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class StockRepository(private val client: HttpClient = ApiClient.client) {
    val token = SessionManager.getAccessToken() ?: throw Exception("Token não disponível")

    suspend fun getStock(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "asc"
    ): List<StockItemDTO> {

        val response: HttpResponse = client.get(("${ApiConfig.baseUrl()}/products")) {
            header("Authorization", "Bearer $token")
            parameter("searchTerm", search)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Erro ao buscar stock: ${response.status}")
        }

        return response.body() // retornará List<StockItemDTO>
    }
}
