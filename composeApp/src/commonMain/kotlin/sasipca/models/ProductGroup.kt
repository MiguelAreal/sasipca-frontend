package sasipca.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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