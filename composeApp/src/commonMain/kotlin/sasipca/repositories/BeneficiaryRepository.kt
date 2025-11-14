package sasipca.repositories

import sasipca.models.*
import sasipca.storage.ApiConfig
import sasipca.utils.NotFoundException
import sasipca.utils.RepositoryException
import io.ktor.client.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.*
import sasipca.storage.requestWithAuth

class BeneficiaryRepository(private val client: HttpClient) {

    suspend fun getProfile(beneficiaryId: Int): BeneficiaryGetDTO {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = "${ApiConfig.baseUrl()}/beneficiaries/$beneficiaryId"
        )
    }

    suspend fun getProfiles(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 20,
        orderBy: String = "asc"
    ): PaginatedResponse<BeneficiaryListDTO> {
        return try {
            client.requestWithAuth(
                method = HttpMethod.Get,
                url = "${ApiConfig.baseUrl()}/beneficiaries?searchTerm=$search&pageNumber=$pageNumber&pageSize=$pageSize&orderBy=$orderBy"
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

    suspend fun postProfile(dto: BeneficiaryPostDTO): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = "${ApiConfig.baseUrl()}/beneficiaries",
            body = dto
        )
    }

    suspend fun updateProfile(beneficiaryId: Int, dto: BeneficiaryPostDTO): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Put,
            url = "${ApiConfig.baseUrl()}/beneficiaries/$beneficiaryId",
            body = dto,
        )
    }
}
