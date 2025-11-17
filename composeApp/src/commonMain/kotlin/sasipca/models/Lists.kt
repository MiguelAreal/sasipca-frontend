package sasipca.models

import kotlinx.serialization.Serializable
import sasipca.ui.components.NamedItem

@Serializable
data class Lists(
    val categories: List<CategoryType>,
    val units: List<UnitTypeInfo>,
    val movements: List<MovementType>,
    val deliveries: List<DeliveriesStatus>,
    val reports: List<ReportTypes>,
    val activeCampaigns: List<ActiveCampaigns>
)

@Serializable
data class CategoryType(val id: Int, val type: String)

@Serializable
data class UnitTypeInfo(val id: Int, val type: String)

@Serializable
data class MovementType(val id: Int, val type: String)

@Serializable
data class DeliveriesStatus(val id: Int, val status: String)

@Serializable
data class ReportTypes(val id: Int, val type: String)

@Serializable
data class ActiveCampaigns(val id: Int, override val name: String) : NamedItem
