package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryCreationDTO(
    val beneficiaryId: Int,
    val scheduledDate: String, // formato ISO: "2025-11-06"
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItemDTO>
)

@Serializable
data class DeliveryUpdateDTO(
    val scheduledDate: String? = null,
    val newStatus: Int? = null, // 1=Agendada, 2=Entregue, 3=Cancelada
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItemDTO>
)

@Serializable
data class DeliveryItemDTO(
    val barcode: String,
    val lot: String,
    val quantity: Int
)

@Serializable
data class DeliveryQueryDTO(
    val statusId: Int? = null,
    val beneficiaryId: Int? = null,
    val dateFrom: String? = null, // formato ISO "2025-01-01"
    val dateTo: String? = null
)

/**
 * Representa uma entrega devolvida pela API (View VDelivery)
 */
@Serializable
data class VDeliveryDTO(
    val deliveryId: Int,
    val scheduledDate: String,
    val status: String,
    val note: String? = null,
    val userId: Int,
    val userName: String? = null,
    val beneficiaryId: Int,
    val beneficiaryName: String? = null,
)
