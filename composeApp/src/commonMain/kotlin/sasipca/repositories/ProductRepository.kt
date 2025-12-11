package sasipca.repositories

import sasipca.models.PaginatedResponse
import sasipca.network.ApiConfig
import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.Product
import sasipca.models.ProductDetail
import sasipca.models.ProductPut
import sasipca.models.Resposta
import sasipca.network.requestWithAuth

class ProductRepository(private val client: HttpClient) {

    /**
     * Busca de lista de produtos de forma paginada e com filtros de pesquisa.
     */
    suspend fun getProducts(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "asc"
    ): PaginatedResponse<Product> {

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("products")
                parameters.append("searchTerm", search)
                parameters.append("pageNumber", "$pageNumber")
                parameters.append("pageSize", "$pageSize")
                parameters.append("orderBy", orderBy)
            }.buildString()
        )
    }

    /**
     * Busca de um produto específico, incluindo detalhes.
     */
    suspend fun getProduct(
        barcode: String = ""
    ): ProductDetail {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("products",barcode)
            }.buildString()
        )
    }

    /**
     * Atualiza um produto existente (nome, categoria, tipo de medida, quantidade)
     */
    suspend fun putProduct(barcode: String, body: ProductPut): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Put,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("products",barcode)
            }.buildString(),
            body = body
        )
    }
}
