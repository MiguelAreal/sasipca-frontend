package sasipca.repositories

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.request.get
import io.ktor.http.*
import sasipca.models.Lists
import sasipca.storage.ApiConfig
import sasipca.storage.ListsStore
import sasipca.storage.requestWithAuth
import sasipca.utils.NotFoundException
import sasipca.utils.RepositoryException

class ListsRepository(private val client: HttpClient) {

    /**
     * Carrega categorias e tipos de unidade da API e guarda no ListsStore.
     */
    suspend fun loadLists() {
        try {
            val result: Lists = client.get("${ApiConfig.baseUrl()}/lists").body()

            // Guardar globalmente
            ListsStore.load(
                categoriesTypes = result.categories,
                unitTypes = result.units,
                movementTypes = result.movements,
                deliveriesStatus = result.deliveries,
                reportTypes = result.reports
            )

        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                throw NotFoundException("Listas não encontradas.")
            } else {
                throw RepositoryException("Erro ao carregar listas: ${e.response.status}")
            }

        } catch (e: Exception) {
            throw RepositoryException("Erro ao carregar listas: ${e.message}")
        }
    }
}
