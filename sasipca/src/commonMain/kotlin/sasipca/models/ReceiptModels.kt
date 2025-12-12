package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptPost(
    val barcode: String,
    val groups: List<ReceiptGroupItem>,
    val name: String? = null,
    val categoryId: Int? = null,
    val unitId: Int? = null,
    val campaignId: Int? = null,
    val unitSize: Int,
    val note: String? = null,
)

@Serializable
data class ReceiptGroupItem(
    val quantity: Int,
    val expiryDate: String, // formato ISO "2025-01-01"
)

// Para UI apenas
data class GroupToEnter(
    val quantity: String,
    val expiryDate: String
)

