package sasipca.models

import kotlinx.datetime.LocalDate
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
    val expNotif: Int? = null,
    val productGroups: List<ProductGroup> = emptyList(),
    /*Imagens vêm sempre de OpenFoodFacts*/
    var images: List<String>? = emptyList()
)

/**
 * Classe que representa grupo/s de um produto
 */
@Serializable
data class ProductGroup(
    val id: Int,
    val expiryDate: LocalDate,
    val totalQuantity: Int,
    val reservedQuantity: Int,
    val availableStock: Int
)

/**
 * Classe que serve para atualizar o cabeçalho de um produto.
 * Só são aplicados os valores que forem na classe, o que não tiver não se mexe
 */
@Serializable
data class ProductPut(
    val name: String ? = null,
    val unitSize: Int? = null,
    val categoryId: Int? = null,
    val unitId: Int? = null,
    val expNotif: Int? = null
)
