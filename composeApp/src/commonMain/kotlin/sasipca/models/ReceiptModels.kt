package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptPost(
    val barcode: String,
    val lots: List<ReceiptLotItem>,
    val name: String? = null,
    val categoryId: Int? = null,
    val unitId: Int? = null,
    val campaignId: Int? = null,
    val unitSize: Int,
    val note: String? = null,
)

@Serializable
data class ReceiptLotItem(
    val lot: String,
    val quantity: Int,
    val expiryDate: String, // formato ISO "2025-01-01"
)

// Para UI apenas
data class LotToEnter(
    val lot: String,
    val quantity: String,
    val expiryDate: String
)

