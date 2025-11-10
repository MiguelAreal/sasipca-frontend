package sasipca.repositories

import sasipca.models.*
import sasipca.storage.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sasipca.utils.NotFoundException
import sasipca.utils.RepositoryException

class BeneficiaryRepository(private val client: HttpClient) {

    suspend fun getProfile(beneficiaryId: Int): BeneficiaryGetDTO {
        return client.get("${ApiConfig.baseUrl()}/beneficiaries/$beneficiaryId").body()
    }

    suspend fun getProfiles(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 20,
        orderBy: String = "asc"
    ): PaginatedResponse<BeneficiaryListDTO> {
        val response: HttpResponse = client.get("${ApiConfig.baseUrl()}/beneficiaries") {
            parameter("searchTerm", search)
            parameter("pageNumber", pageNumber)
            parameter("pageSize", pageSize)
            parameter("orderBy", orderBy)
        }

        return when {
            response.status.value == 404 -> throw NotFoundException("Nenhum beneficiário encontrado.")
            !response.status.isSuccess() -> throw RepositoryException("Erro ao buscar beneficiários: ${response.status}")
            else -> response.body()
        }
    }

    suspend fun postProfile(dto: BeneficiaryPostDTO): Resposta {
        return client.post("${ApiConfig.baseUrl()}/beneficiaries") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    suspend fun updateProfile(beneficiaryId: Int, dto: BeneficiaryPostDTO): Resposta {
        return client.put("${ApiConfig.baseUrl()}/beneficiaries/$beneficiaryId") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }
}
