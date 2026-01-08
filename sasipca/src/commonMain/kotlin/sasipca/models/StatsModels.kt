package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummary(
    val totalProductsInStock: Int,
    val pendingDeliveriesCount: Int,
    val expiredStockQuantity: Int,
    val newBeneficiariesCount: Int
)

@Serializable
data class ChartDataPoint(
    val label: String,
    val value: Double,
    val series: String? = null
)

@Serializable
data class MonthlySummary(
    val month: Int,
    val year: Int,
    val pendingDeliveries: Int,
    val realizedDeliveries: Int,
    val donationsReceived: Int
)
