package sasipca.models

import kotlinx.serialization.Serializable

/**
 * Classe que representa um produto completo.
 */
@Serializable
data class ProductDetail(
    val barcode: String,
    val name: String,
    val unitSize: Int? = null,
    val categoryId: Int?,
    val unitId: Int?,
    val totalQuantity: Int? = null,
    val reservedQuantity: Int? = null,
    val availableStock: Int? = null,
    val productLots: List<ProductLot> = emptyList(),
    /*Imagens vêm sempre de OpenFoodFacts*/
    var images: List<String>? = emptyList()
)
