package sasipca.repositories

import sasipca.models.ProductOFFResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import sasipca.utils.UnitConverter

class OFFRepository(private val client: HttpClient) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val oofUrl = "https://world.openfoodfacts.org/api/v2/product"

    /**
     * Pesquisa produto pelo código de barras (EAN-13)
     * Retorna null se não for encontrado.
     */
    suspend fun getProduct(barcode: String): ProductOFFResponse? {
        return try {
            val response: HttpResponse = client.get("${oofUrl}/$barcode")

            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                val productResponse = json.decodeFromString<ProductOFFResponse>(body)

                productResponse.product?.let { p ->
                    val normalized = UnitConverter.normalize(
                        p.product_quantity,
                        p.product_quantity_unit
                    )

                    if (normalized != null) {
                        // Atualiza diretamente o modelo original
                        p.product_quantity = normalized.value
                        p.product_quantity_unit = normalized.unit
                    }
                }

                productResponse
            } else {
                null
            }
        } catch (e: Exception) {
            println("Erro ao consultar OpenFoodFacts: ${e.message}")
            null
        }
    }

}
