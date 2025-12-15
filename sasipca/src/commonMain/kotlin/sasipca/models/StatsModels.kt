package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummary(
    val totalProductsInStock: Int,
    val lowStockCount: Int,
    val pendingDeliveriesCount: Int,
    val activeBeneficiariesCount: Int
)

@Serializable
data class ChartDataPoint(
    val label: String,
    val value: Double,
    val series: String? = null
)

// Filtro opcional para enviar ao backend
data class DateRangeFilter(
    val dateFrom: String? = null, // yyyy-MM-dd
    val dateTo: String? = null
)

@Serializable
data class MonthlySummary(
    val month: Int,
    val year: Int,
    val pendingDeliveries: Int,
    val realizedDeliveries: Int,
    val donationsReceived: Int
)
