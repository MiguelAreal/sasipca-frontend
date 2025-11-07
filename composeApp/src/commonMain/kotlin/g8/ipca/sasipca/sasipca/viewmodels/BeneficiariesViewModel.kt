package g8.ipca.sasipca.sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import g8.ipca.sasipca.sasipca.models.BeneficiaryListDTO
import g8.ipca.sasipca.sasipca.models.PaginatedResponse
import g8.ipca.sasipca.sasipca.repositories.BeneficiaryRepository
import g8.ipca.sasipca.sasipca.repositories.ProductRepository
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel responsável por gerir o estado da lista de beneficiários
 * (filtragem, paginação, loading e erros)
 */

class BeneficiariesViewModel(private val repository: BeneficiaryRepository) : ViewModel() {

    // region States observáveis
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var beneficiaries by mutableStateOf<PaginatedResponse<BeneficiaryListDTO>?>(null)
        private set

    var searchTerm by mutableStateOf("")
        private set

    var orderBy by mutableStateOf("asc")
        private set

    var currentPage by mutableStateOf(1)
        private set

    private val pageSize = 10
    // endregion


    /**
     * Carrega beneficiários (paginação, filtro e ordenação)
     */
    fun loadBeneficiaries(
        search: String = searchTerm,
        page: Int = currentPage,
        order: String = orderBy
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                errorMessage = null

                val response: PaginatedResponse<BeneficiaryListDTO> =
                    repository.getProfiles(
                        search = search,
                        pageNumber = page,
                        pageSize = pageSize,
                        orderBy = order
                    )

                val mapped = PaginatedResponse<BeneficiaryListDTO>(
                    data = response.data.map {
                        BeneficiaryListDTO(
                            beneficiaryId = it.beneficiaryId,
                            name = it.name,
                            email = it.email
                        )
                    },
                    pageNumber = response.pageNumber,
                    pageSize = response.pageSize,
                    totalCount = response.totalCount,
                    totalPages = response.totalPages
                )

                beneficiaries = mapped
                currentPage = page
                searchTerm = search
                orderBy = order
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erro ao carregar beneficiários."
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
