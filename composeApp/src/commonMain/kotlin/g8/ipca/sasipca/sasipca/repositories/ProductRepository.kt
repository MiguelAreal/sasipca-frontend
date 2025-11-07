package g8.ipca.sasipca.sasipca.repositories

import g8.ipca.sasipca.sasipca.models.PaginatedResponse
import g8.ipca.sasipca.sasipca.models.ProductItemDTO
import g8.ipca.sasipca.sasipca.storage.ApiConfig
import g8.ipca.sasipca.sasipca.storage.authorizedRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ProductRepository(private val client: HttpClient) {
    suspend fun getProducts(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "asc"
    ): PaginatedResponse<ProductItemDTO> {

        val response: HttpResponse = client.authorizedRequest(
            url = "${ApiConfig.baseUrl()}/products",
            method = HttpMethod.Get
        ) {
            parameter("searchTerm", search)
            parameter("pageNumber", pageNumber)
            parameter("pageSize", pageSize)
            parameter("orderBy", orderBy)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Erro ao buscar produtos: ${response.status}")
        }

        return response.body<PaginatedResponse<ProductItemDTO>>()
    }
}
