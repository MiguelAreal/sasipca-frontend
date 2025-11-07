package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductItemDTO(
    val barcode: String,
    val name: String,
    val category: String,
    val unit: String? = null,
    val unitSize: Int? = null,
    val totalQuantity: Int? = null,
    val reservedQuantity: Int? = null,
    val availableStock: Int? = null
)
