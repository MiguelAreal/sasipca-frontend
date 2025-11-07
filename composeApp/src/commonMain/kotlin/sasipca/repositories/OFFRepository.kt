package sasipca.repositories

import sasipca.models.ProductOFFResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class OFFRepository(private val client: HttpClient) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val baseUrl = "https://world.openfoodfacts.org/api/v2/product"

    /**
     * Pesquisa produto pelo código de barras (EAN-13)
     * Retorna null se não for encontrado.
     */
    suspend fun getProductByBarcode(barcode: String): ProductOFFResponse? {
        return try {
            val response: HttpResponse = client.get("https://world.openfoodfacts.org/api/v2/product/$barcode")

            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                json.decodeFromString<ProductOFFResponse>(body)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Erro ao consultar OpenFoodFacts: ${e.message}")
            null
        }
    }
}
