package sasipca.repositories

import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import sasipca.models.Campaign
import sasipca.models.PaginatedResponse
import sasipca.models.Resposta
import sasipca.storage.ApiConfig
import sasipca.storage.requestWithAuth

class CampaignRepository(private val client: HttpClient) {

    /**
     * GET: Lista campanhas de forma paginada e filtrada.
     */
    suspend fun getCampaigns(
        pageNumber: Int = 1,
        pageSize: Int = 10,
        orderBy: String = "desc",
        searchTerm: String = ""
    ): PaginatedResponse<Campaign> {

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("campaigns")
                parameters.append("pageNumber", pageNumber.toString())
                parameters.append("pageSize", pageSize.toString())
                parameters.append("orderBy", orderBy)
                parameters.append("searchTerm", searchTerm)
            }.buildString()
        )
    }

    /**
     * GET: Detalhe de uma campanha.
     */
    suspend fun getCampaign(id: Int): Campaign {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("campaigns", "$id")
            }.buildString()
        )
    }

    /**
     * POST: Criar Campanha.
     */
    suspend fun createCampaign(
        name: String,
        description: String?,
        location: String?,
        startDate: String,
        endDate: String,
        imageBytes: ByteArray?,
        imageFileName: String?
    ): Resposta {
        val data = formData {
            append("Name", name)
            append("StartDate", startDate)
            append("EndDate", endDate)
            if (description != null) append("Description", description)
            if (location != null) append("Location", location)

            if (imageBytes != null && imageFileName != null) {
                append("ImageFile", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$imageFileName\"")
                })
            }
        }

        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("campaigns")
            }.buildString(),
            formData = data
        )
    }

    /**
     * PUT: Atualizar Campanha.
     */
    suspend fun updateCampaign(
        id: Int,
        name: String,
        description: String?,
        location: String?,
        startDate: String?,
        endDate: String?,
        newImageBytes: ByteArray?,
        newImageFileName: String?,
        removeImage: Boolean
    ): Resposta {
        val data = formData {
            append("Name", name)
            if (startDate != null) append("StartDate", startDate)
            if (endDate != null) append("EndDate", endDate)
            if (description != null) append("Description", description)
            if (location != null) append("Location", location)

            append("RemoveImage", removeImage.toString())

            if (newImageBytes != null && newImageFileName != null) {
                append("NewImageFile", newImageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$newImageFileName\"")
                })
            }
        }

        return client.requestWithAuth(
            method = HttpMethod.Put,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("campaigns", "$id")
            }.buildString(),
            formData = data
        )
    }

    /**
     * DELETE: Eliminar Campanha
     */
    suspend fun deleteCampaign(id: Int): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Delete,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("campaigns", "$id")
            }.buildString()
        )
    }
}