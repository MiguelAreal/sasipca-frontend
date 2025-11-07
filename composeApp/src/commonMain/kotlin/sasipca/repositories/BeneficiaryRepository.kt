package sasipca.repositories

import sasipca.models.BeneficiaryGetDTO
import sasipca.models.BeneficiaryListDTO
import sasipca.models.BeneficiaryPostDTO
import sasipca.models.PaginatedResponse
import sasipca.models.Resposta
import sasipca.storage.ApiConfig
import sasipca.storage.authorizedRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Repository responsável por gerir todas as operações relacionadas com beneficiários
 */
class BeneficiaryRepository(private val client: HttpClient) {

    /**
     * Busca apenas um perfil de beneficiário, consoante ID de beneficiário
     */
    suspend fun getProfile(beneficiaryId: Int): BeneficiaryGetDTO {
        return client.authorizedRequest<BeneficiaryGetDTO>(
            url = "${ApiConfig.baseUrl()}/beneficiaries/$beneficiaryId",
            method = HttpMethod.Get
        )
    }

    /**
     * Busca uma lista paginada de perfis de beneficiários, consoante filtros de pesquisa
     */
    suspend fun getProfiles(
        search: String = "",
        pageNumber: Int = 1,
        pageSize: Int = 20,
        orderBy: String = "asc"
    ): PaginatedResponse<BeneficiaryListDTO> {

        val response: HttpResponse = client.authorizedRequest(
            url = "${ApiConfig.baseUrl()}/beneficiaries",
            method = HttpMethod.Get
        ) {
            parameter("searchTerm", search)
            parameter("pageNumber", pageNumber)
            parameter("pageSize", pageSize)
            parameter("orderBy", orderBy)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Erro ao buscar produtos: ${response.status}")
        }
        return response.body<PaginatedResponse<BeneficiaryListDTO>>()
    }


    /**
     *  Cria um novo perfil de beneficiário
     */
    suspend fun postProfile(dto: BeneficiaryPostDTO): Resposta {
        return client.authorizedRequest(
            url = "${ApiConfig.baseUrl()}/beneficiaries",
            method = HttpMethod.Post
        ) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }

    /**
     * Atualiza o perfil de um beneficiário
     */
    suspend fun updateProfile(beneficiaryId: Int, dto: BeneficiaryPostDTO): Resposta {
        return client.authorizedRequest<Resposta>(
            url = "${ApiConfig.baseUrl()}/beneficiaries/$beneficiaryId",
            method = HttpMethod.Put
        ) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }
}
