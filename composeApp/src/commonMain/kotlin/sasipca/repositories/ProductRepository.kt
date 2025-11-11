package sasipca.repositories

import sasipca.models.PaginatedResponse
import sasipca.models.ProductItemDTO
import sasipca.storage.ApiConfig
import io.ktor.client.*
import io.ktor.http.*
import sasipca.storage.requestWithAuth

class ProductRepository(private val client: HttpClient) {

    suspend fun getProducts(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "asc"
    ): PaginatedResponse<ProductItemDTO> {

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = "${ApiConfig.baseUrl()}/products?searchTerm=$search&pageNumber=$pageNumber&pageSize=$pageSize&orderBy=$orderBy"
        )
    }
}
