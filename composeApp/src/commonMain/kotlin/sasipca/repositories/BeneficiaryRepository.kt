package sasipca.repositories

import sasipca.models.*
import sasipca.storage.ApiConfig
import sasipca.utils.NotFoundException
import sasipca.utils.RepositoryException
import io.ktor.client.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.*
import io.ktor.http.URLBuilder
import sasipca.storage.requestWithAuth

class BeneficiaryRepository(private val client: HttpClient) {

    suspend fun getProfile(beneficiaryId: Int): BeneficiaryGet {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
            appendPathSegments("beneficiaries",beneficiaryId.toString())
        }.buildString()
        )
    }

    suspend fun getProfiles(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 20,
        orderBy: String = "asc"
    ): PaginatedResponse<BeneficiaryItem> {

        val url = URLBuilder(ApiConfig.baseUrl()).apply {
            appendPathSegments("beneficiaries")

            parameters.append("searchTerm", search)
            parameters.append("pageNumber", pageNumber.toString())
            parameters.append("pageSize", pageSize.toString())
            parameters.append("orderBy", orderBy)
        }.buildString()

        return try {
            client.requestWithAuth(
                method = HttpMethod.Get,
                url = url
            )
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                throw NotFoundException("Nenhum beneficiário encontrado.")
            } else {
                throw RepositoryException("Erro ao buscar beneficiários: ${e.response.status}")
            }
        } catch (e: Exception) {
            throw RepositoryException("Erro ao buscar beneficiários: ${e.message}")
        }
    }


    suspend fun postProfile(body: BeneficiaryPost): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
            appendPathSegments("beneficiaries")
        }.buildString(),
            body = body
        )
    }

    suspend fun putProfile(beneficiaryId: Int, body: BeneficiaryPost): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Put,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("beneficiaries",beneficiaryId.toString())
            }.buildString(),
            body = body,
        )
    }
}
