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
    val product: Product? = null
)

@Serializable
data class Product(
    val product_name: String? = null,
    var product_quantity: Double? = null,
    var product_quantity_unit: String? = null,
    val image_url: String? = null,

    val brands: String? = null
)
