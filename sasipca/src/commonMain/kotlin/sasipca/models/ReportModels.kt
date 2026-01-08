package sasipca.models

import kotlinx.serialization.Serializable

// Enums iguais ao Backend
enum class ReportTypesEnum(val value: Int) {
    MovementHeaders(1),
    MovementDetails(2), // Requer ID
    DeliveryHeaders(3);

    fun label(): String {
        return when(this) {
            MovementHeaders -> "Resumo de Movimentos"
            MovementDetails -> "Detalhe de Movimento"
            DeliveryHeaders -> "Resumo de Entregas"
        }
    }
}

enum class ReportFormat(val value: Int) {
    PDF(1),
    CSV(2);
}

// DTO para GET (Listagem)
@Serializable
data class ReportGetDTO(
    val id: Int,
    val name: String,
    val creatorName: String,
    val reportTypeId: Int,
    val reportTypeName: String,
    val createdAt: String // Vem como string ISO do backend
)

// DTO para POST (Gerar novo)
@Serializable
data class ReportRequestDTO(
    val type: Int,       // ReportTypesEnum.value
    val format: Int,     // ReportFormat.value
    val fileName: String,
    val filters: ReportFiltersDTO? = null,
    val targetMovementId: Int? = null
)

@Serializable
data class ReportFiltersDTO(
    val startDate: String? = null, // yyyy-MM-dd
    val endDate: String? = null,   // yyyy-MM-dd
    val status: Int? = null,
    val beneficiaryId: Int? = null
)