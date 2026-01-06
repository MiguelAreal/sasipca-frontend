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
    val productName: String? = null,
    var productQuantity: Int? = null,
    var productQuantityUnit: String? = null,
    val imageUrl: String? = null,
    val imageIngredientsUrl: String? = null,
    val imagePackagingUrl: String? = null,
    val brands: String? = null
) {
    // Lista de imagens que junta todas as imagens do objeto
    val images: List<String>
        get() = listOfNotNull( imageUrl, imageIngredientsUrl, imagePackagingUrl)
}



