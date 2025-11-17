package sasipca.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Classe que representa lote/s de um produto
 */
@Serializable
data class ProductLot(
    val id: Int,
    val lot: String,
    val expiryDate: LocalDate,
    val totalQuantity: Int,
    val reservedQuantity: Int,
    val availableStock: Int
)