package sasipca.repositories

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.request.get
import io.ktor.http.*
import sasipca.models.Lists
import sasipca.models.SasipcaApiException
import sasipca.network.ApiConfig
import sasipca.storage.ListsStore
import sasipca.utils.NotFoundException
import sasipca.utils.RepositoryException
import sasipca.network.requestWithAuth

class ListsRepository(private val client: HttpClient) {

    /**
     * Carrega categorias e tipos de unidade da API e guarda no ListsStore.
     */
    suspend fun loadLists() {
        try {
            val result: Lists = client.requestWithAuth(
                method = HttpMethod.Get,
                url = "${ApiConfig.baseUrl()}/lists"
            )

            // Guardar globalmente
            ListsStore.load(
                categoriesTypes = result.categories,
                unitTypes = result.units,
                movementTypes = result.movements,
                deliveriesStatus = result.deliveries,
                reportTypes = result.reports,
                activeCampaigns = result.activeCampaigns
            )

        } catch (e: SasipcaApiException) {
            throw e
        } catch (e: Exception) {
            // Outros erros de rede ou parsing
            throw RepositoryException("Erro ao carregar listas: ${e.message}")
        }
    }
}
