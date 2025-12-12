package sasipca.models

import kotlinx.serialization.Serializable

// --- LISTAGEM DE MOVIMENTOS ---
@Serializable
data class MovementHistory(
    val movementId: Int,
    val movementDate: String, // DateTime do C# vem como String ISO
    val movementTypeId: Int,
    val movementNote: String?,
    val userId: Int,
    val userName: String?,
    val deliveryId: Int?,
    val totalQuantityAffected: Double?
)

// --- DETALHE DE MOVIMENTO ---
@Serializable
data class MovementDetail(
    val movementId: Int,
    val movementDate: String,
    val movementTypeId: Int,
    val movementNote: String?,
    val userName: String?,
    val deliveryId: Int?,
    val items: List<MovementItem>
)

@Serializable
data class MovementItem(
    val itemQuantityAffected: Int,
    val productBarcode: String,
    val productName: String,
    val productGroupId: Int,
    val groupExpiryDate: String // DateOnly
)

// --- LISTAGEM DE ENTREGAS (View VDelivery) ---
@Serializable
data class DeliveryHistory(
    val deliveryId: Int,
    val scheduledDate: String,
    val statusId: Int,
    val note: String?,
    val userId: Int,
    val userName: String?,
    val beneficiaryId: Int,
    val beneficiaryName: String
)



// --- ENUMS AUXILIARES PARA UI ---
enum class HistoryTab {
    MOVEMENTS, DELIVERIES
}

fun getMovementTypeName(id: Int): String {
    return when(id) {
        1 -> "Entrada"
        2 -> "Saída"
        3 -> "Ajuste"
        else -> "Desconhecido"
    }
}

fun getDeliveryStatusName(id: Int): String {
    return when(id) {
        1 -> "Agendada"
        2 -> "Entregue"
        3 -> "Cancelada"
        else -> "-"
    }
}