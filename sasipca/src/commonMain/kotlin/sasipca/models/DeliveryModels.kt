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
    val newStatusId: Int,
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItem> // Obrigatório
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

/**
 * Representa o detalhe COMPLETO de uma entrega (GET /api/deliveries/{id})
 */
@Serializable
data class DeliveryDetail(
    val deliveryId: Int,
    val scheduledDate: String,
    val statusId: Int,
    val note: String? = null,
    val userId: Int,
    val userName: String? = null,
    val beneficiaryId: Int?,
    val beneficiaryName: String? = null,
    val items: List<DeliveryItemGetDTO> = emptyList()
)

/**
 * Item individual da entrega (vindo do Backend)
 */
@Serializable
data class DeliveryItemGetDTO(
    val name: String,
    val expiryDate: String? = null, // Vem como "yyyy-mm-dd" do DateOnly serializado
    val quantity: Int,
    val barcode: String? = null,
    val groupId: Int? = null
)
