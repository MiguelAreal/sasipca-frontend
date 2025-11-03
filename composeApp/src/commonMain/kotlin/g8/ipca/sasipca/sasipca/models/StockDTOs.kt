package g8.ipca.sasipca.sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class StockItemDTO(
    val barcode: String,
    val name: String,
    val category: String,
    val unit: String? = null,
    val unitSize: Int? = null,
    val totalQuantity: Int? = null,
    val reservedQuantity: Int? = null,
    val availableStock: Int? = null
)

@Serializable
data class PaginatedStock(
    val data: List<StockItemDTO>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)
