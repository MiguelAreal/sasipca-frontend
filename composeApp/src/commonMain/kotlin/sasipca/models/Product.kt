package sasipca.models

import kotlinx.serialization.Serializable


/**
 * Classe que representa um cabeçalho de produto
 */
@Serializable
data class Product(
    val barcode: String,
    val name: String,
    val categoryId: Int,
    val unitId: Int,
    val unitSize: Int? = null,
    val totalQuantity: Int? = null,
    val reservedQuantity: Int? = null,
    val availableStock: Int? = null
)
