package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.ChartDataPoint
import sasipca.models.DashboardSummary
import sasipca.models.MonthlySummary
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

class StatsRepository(private val client: HttpClient) {

    suspend fun getSummary(): DashboardSummary {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = "${ApiConfig.baseUrl()}/stats/summary"
        )
    }

    suspend fun getMovementsFlow(dateFrom: String? = null, dateTo: String? = null): List<ChartDataPoint> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stats", "movements-flow")
                if (dateFrom != null) parameters.append("dateFrom", dateFrom)
                if (dateTo != null) parameters.append("dateTo", dateTo)
            }.buildString()
        )
    }

    suspend fun getTopProducts(dateFrom: String? = null, dateTo: String? = null, topN: Int = 5): List<ChartDataPoint> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stats", "top-products")
                if (dateFrom != null) parameters.append("dateFrom", dateFrom)
                if (dateTo != null) parameters.append("dateTo", dateTo)
                parameters.append("topN", topN.toString())
            }.buildString()
        )
    }

    suspend fun getCategoriesDistribution(dateFrom: String? = null, dateTo: String? = null): List<ChartDataPoint> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stats", "categories-distribution")
                if (dateFrom != null) parameters.append("dateFrom", dateFrom)
                if (dateTo != null) parameters.append("dateTo", dateTo)
            }.buildString()
        )
    }

    suspend fun getMonthlySummary(month: Int, year: Int): MonthlySummary {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stats", "monthly-summary")
                parameters.append("month", month.toString())
                parameters.append("year", year.toString())
            }.buildString()
        )
    }
}