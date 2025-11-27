package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryPost(
    val beneficiaryId: Int,
    val scheduledDate: String, // formato ISO: "2025-11-06"
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItem>
)

@Serializable
data class DeliveryPut(
    val scheduledDate: String? = null,
    val newStatusId: Int? = null, // 1=Agendada, 2=Entregue, 3=Cancelada
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItem>
)

@Serializable
data class DeliveryItem(
    val barcode: String,
    val groupId: Int,
    val quantity: Int
)

@Serializable
data class DeliveryGet(
    val statusId: Int? = null,
    val beneficiaryId: Int? = null,
    val dateFrom: String? = null, // formato ISO "2025-01-01"
    val dateTo: String? = null
)

/**
 * Representa uma entrega vinda da API (View VDelivery)
 */
@Serializable
data class Delivery(
    val deliveryId: Int,
    val scheduledDate: String,
    val statusId: Int,
    val note: String? = null,
    val userId: Int,
    val userName: String? = null,
    val beneficiaryId: Int,
    val beneficiaryName: String? = null,
)
