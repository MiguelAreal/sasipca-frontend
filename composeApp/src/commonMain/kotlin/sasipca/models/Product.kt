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


/**
 * Classe utilizada para mostrar um cabeçalho de produto no front-end.
 *
 * Traduz categoryID e unitID para nomes.
 */
@Serializable
data class ProductUI(
    val barcode: String,
    val name: String,
    val categoryName: String,
    val unitName: String,
    val unitSize: Int?,
    val totalQuantity: Int?,
    val reservedQuantity: Int?,
    val availableStock: Int?
)
