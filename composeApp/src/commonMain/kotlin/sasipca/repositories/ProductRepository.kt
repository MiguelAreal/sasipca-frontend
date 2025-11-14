package sasipca.repositories

import sasipca.models.PaginatedResponse
import sasipca.storage.ApiConfig
import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.ProductItem
import sasipca.storage.requestWithAuth

class ProductRepository(private val client: HttpClient) {

    suspend fun getProducts(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "asc"
    ): PaginatedResponse<ProductItem> {

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = "${ApiConfig.baseUrl()}/products?searchTerm=$search&pageNumber=$pageNumber&pageSize=$pageSize&orderBy=$orderBy"
        )
    }
}
