package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductItem(
    val barcode: String,
    val name: String,
    val categoryId: Int,
    val unitId: Int,
    val unitSize: Int? = null,
    val totalQuantity: Int? = null,
    val reservedQuantity: Int? = null,
    val availableStock: Int? = null
)

@Serializable
data class ProductItemUI(
    val barcode: String,
    val name: String,
    val categoryName: String,
    val unitName: String,
    val unitSize: Int?,
    val totalQuantity: Int?,
    val reservedQuantity: Int?,
    val availableStock: Int?
)
