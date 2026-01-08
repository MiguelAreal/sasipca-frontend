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
    @SerialName("product_name")
    val productName: String? = null,
    @SerialName("product_quantity")
    var productQuantity: Int? = null,
    @SerialName("product_quantity_unit")
    var productQuantityUnit: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("image_ingredients_url")
    val imageIngredientsUrl: String? = null,
    @SerialName("image_packaging_url")
    val imagePackagingUrl: String? = null,
    val brands: String? = null
) {
    // Lista de imagens que junta todas as imagens do objeto
    val images: List<String>
        get() = listOfNotNull( imageUrl, imageIngredientsUrl, imagePackagingUrl)
}



