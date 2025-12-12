package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.ReportGetDTO
import sasipca.models.ReportRequestDTO
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

class ReportsRepository(private val client: HttpClient) {

    /**
     * Lista os relatórios gerados.
     * T -> List<ReportGetDTO> (Parse de JSON)
     */
    suspend fun getGeneratedReports(type: Int? = null): List<ReportGetDTO> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("reports")
                if (type != null) parameters.append("reportType", type.toString())
            }.buildString()
        )
    }

    /**
     * Gera um novo relatório e retorna os bytes.
     * T -> ByteArray (Leitura de binário)
     * * Como usamos requestWithAuth, se o token expirou a meio,
     * ele faz refresh e tenta gerar o relatório novamente sozinho.
     */
    suspend fun generateReport(request: ReportRequestDTO): ByteArray {
        return client.requestWithAuth<ByteArray>(
            method = HttpMethod.Post,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("reports")
            }.buildString(),
            body = request
        )
    }

    /**
     * Faz download de um relatório existente.
     * T -> ByteArray (Leitura de binário)
     */
    suspend fun downloadReport(id: Int): ByteArray {
        return client.requestWithAuth<ByteArray>(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("reports", id.toString())
            }.buildString()
        )
    }
}