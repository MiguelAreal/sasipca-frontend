package sasipca.repositories

import sasipca.models.PaginatedResponse
import sasipca.models.ProductItemDTO
import sasipca.storage.ApiConfig
import sasipca.storage.authorizedRequest
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
