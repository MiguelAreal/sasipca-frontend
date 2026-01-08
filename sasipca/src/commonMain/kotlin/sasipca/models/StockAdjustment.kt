package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class StockAdjustment(
    val barcode: String,
    val groupId: Int,
    val quantityAdjustment: Int,
    val note: String
)