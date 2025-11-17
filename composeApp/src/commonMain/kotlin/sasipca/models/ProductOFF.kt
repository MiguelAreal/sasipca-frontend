package sasipca.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Para produto vindo do OpenFoodFacts
 */
@Serializable
data class ProductOFFResponse(
    val status: Int,
    @SerialName("status_verbose") val statusVerbose: String,
    val code: String,
    val product: ProductOFF? = null
)

@Serializable
data class ProductOFF(
    val product_name: String? = null,
    var product_quantity: Int? = null,
    var product_quantity_unit: String? = null,
    val image_url: String? = null,
    val image_ingredients_url: String? = null,
    val image_packaging_url: String? = null,
    val brands: String? = null
) {
    // Lista de imagens que junta todas as imagens do objeto
    val images: List<String>
        get() = listOfNotNull( image_url, image_ingredients_url, image_packaging_url)
}



