package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.ChartDataPoint
import sasipca.models.DashboardSummary
import sasipca.models.MonthlySummary
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

class StatsRepository(private val client: HttpClient) {

    // 1. Resumo Global (KPIs)
    suspend fun getSummary(dateFrom: String? = null, dateTo: String? = null): DashboardSummary {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stats", "summary")
                if (dateFrom != null) parameters.append("dateFrom", dateFrom)
                if (dateTo != null) parameters.append("dateTo", dateTo)
            }.buildString()
        )
    }

    // 2. Fluxo de Movimentos
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

    // 3. Top Produtos (Saída)
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

    // 4. Distribuição por Categorias (CONSOLIDADO)
    // movementTypeId: 1 = Entrada, 2 = Saída (Iguais ao Enum do C#)
    suspend fun getCategoriesDistribution(movementTypeId: Int, dateFrom: String? = null, dateTo: String? = null): List<ChartDataPoint> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stats", "categories-distribution")
                parameters.append("movementTypeId", movementTypeId.toString()) // Novo parâmetro
                if (dateFrom != null) parameters.append("dateFrom", dateFrom)
                if (dateTo != null) parameters.append("dateTo", dateTo)
            }.buildString()
        )
    }

    // 5. Resumo Mensal (Para a ‘Homepage’)
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