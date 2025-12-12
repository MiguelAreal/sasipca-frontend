package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import sasipca.models.AdminUser
import sasipca.models.PaginatedResponse
import sasipca.models.PostAdmin
import sasipca.models.Resposta
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

class AdminRepository(private val client: HttpClient) {

    /**
     * Listar todos os admins com paginação e pesquisa.
     */
    suspend fun getAdmins(
        page: Int = 1,
        pageSize: Int = 10,
        search: String = ""
    ): PaginatedResponse<AdminUser> {

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("admins")
                parameters.append("pageNumber", "$page")
                parameters.append("pageSize", "$pageSize")
                parameters.append("searchTerm", search)
            }.buildString()
        )
    }

    /**
     * Criar novo admin.
     * Retorna o objeto Resposta da API.
     */
    suspend fun createAdmin(email: String, contact: String): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("admins")
            }.buildString(),
            body = PostAdmin(email, contact)
        )
    }
}