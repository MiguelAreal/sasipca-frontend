package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.Campaign
import sasipca.repositories.CampaignRepository
import sasipca.repositories.ListsRepository
import java.time.LocalDate

// --- Estado do Formulário ---
data class CampaignFormState(
    val id: Int = 0,
    val name: String = "",
    val location: String = "",
    val startDate: String = "", // Formato API: yyyy-MM-dd
    val endDate: String = "",   // Formato API: yyyy-MM-dd
    val description: String = "",
    val imageUrl: String? = null,         // URL existente
    val newImageBytes: ByteArray? = null, // Nova imagem selecionada
    val removeImage: Boolean = false,     // Flag para apagar a imagem no backend
    val errors: Map<String, String> = emptyMap()
)

// --- Estado da UI (Feedback global) ---
data class CampaignUiState(
    val isLoading: Boolean = false,
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class CampaignViewModel(
    private val campaignRepository: CampaignRepository,
    private val listsRepository: ListsRepository
) : ViewModel() {

    // --- Estados de Feedback ---
    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState

    // --- Estado do Formulário ---
    var formState by mutableStateOf(CampaignFormState())
        private set

    var isDialogOpen by mutableStateOf(false)
        private set

    // --- Estados da Lista (Paginação e Pesquisa) ---
    var filteredItems by mutableStateOf<List<Campaign>>(emptyList())
        private set
    var searchQuery by mutableStateOf("")
        private set
    var currentPage by mutableStateOf(1)
        private set
    private val pageSize = 10
    var totalPages by mutableStateOf(1)
        private set
    var isLoading by mutableStateOf(false)
        private set

    // =========================================================================
    // LÓGICA DE LISTAGEM
    // =========================================================================

    fun loadCampaigns(query: String = searchQuery) {
        if (query != searchQuery) {
            currentPage = 1 // Reset para página 1 se a pesquisa mudar
        }
        searchQuery = query
        isLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = campaignRepository.getCampaigns(
                    pageNumber = currentPage,
                    pageSize = pageSize,
                    orderBy = "desc",
                    searchTerm = searchQuery
                )
                filteredItems = response.data
                totalPages = response.totalPages
                currentPage = response.pageNumber // Sincronizar com servidor
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    lastErrorMessage = e.message ?: "Erro ao carregar campanhas"
                )
                filteredItems = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun goToNextPage() {
        if (currentPage < totalPages && !isLoading) {
            currentPage++
            loadCampaigns()
        }
    }

    fun goToPreviousPage() {
        if (currentPage > 1 && !isLoading) {
            currentPage--
            loadCampaigns()
        }
    }

    // =========================================================================
    // LÓGICA DE GESTÃO DO FORMULÁRIO
    // =========================================================================

    /**
     * Prepara o formulário para editar uma campanha existente.
     */
    fun selectCampaignToEdit(campaign: Campaign) {
        formState = CampaignFormState(
            id = campaign.id,
            name = campaign.name,
            location = campaign.location ?: "",
            startDate = campaign.startDate,
            endDate = campaign.endDate,
            description = campaign.description ?: "",
            imageUrl = campaign.imageUrl,
            removeImage = false,
            newImageBytes = null
        )
        isDialogOpen = true
    }

    /**
     * Prepara o formulário para uma nova campanha (datas default).
     */
    fun startNewCampaign() {
        val today = LocalDate.now().toString()
        val nextWeek = LocalDate.now().plusDays(7).toString()

        formState = CampaignFormState(
            startDate = today,
            endDate = nextWeek,
            removeImage = false
        )
        isDialogOpen = true
    }

    fun closeDialog() {
        isDialogOpen = false
        formState = CampaignFormState() // Limpa o estado para evitar lixo na próxima abertura
    }

    // --- Inputs do Utilizador ---

    fun onNameChange(newName: String) {
        formState = formState.copy(name = newName)
        validateField("name", newName)
    }

    fun onLocationChange(newLocation: String) {
        formState = formState.copy(location = newLocation)
    }

    fun onDescriptionChange(newDesc: String) {
        formState = formState.copy(description = newDesc)
    }

    // Recebe data em formato UI (dd/MM/yyyy) e converte para API (yyyy-MM-dd)
    fun onStartDateChange(uiDate: String) {
        val apiDate = convertDateToApi(uiDate)
        formState = formState.copy(startDate = apiDate)
        validateDates()
    }

    fun onEndDateChange(uiDate: String) {
        val apiDate = convertDateToApi(uiDate)
        formState = formState.copy(endDate = apiDate)
        validateDates()
    }

    // --- Gestão de Imagens ---

    fun onImagePicked(bytes: ByteArray?) {
        if (bytes != null) {
            // Se escolheu uma nova imagem, guardamos os bytes e garantimos que NÃO removemos
            formState = formState.copy(newImageBytes = bytes, removeImage = false)
        }
    }

    fun onRemoveImage() {
        // Marca para remoção e limpa quaisquer bytes novos que tivessem sido escolhidos
        formState = formState.copy(removeImage = true, newImageBytes = null)
    }

    // =========================================================================
    // LÓGICA DE SUBMISSÃO (CRUD)
    // =========================================================================

    fun saveCampaign() {
        if (!validateAll()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CampaignUiState(isLoading = true)
            try {
                if (formState.id == 0) {
                    // --- CREATE ---
                    campaignRepository.createCampaign(
                        name = formState.name,
                        description = formState.description,
                        location = formState.location,
                        startDate = formState.startDate,
                        endDate = formState.endDate,
                        imageBytes = formState.newImageBytes,
                        imageFileName = if (formState.newImageBytes != null) "campaign.jpg" else null
                    )
                } else {
                    // --- UPDATE ---
                    campaignRepository.updateCampaign(
                        id = formState.id,
                        name = formState.name,
                        description = formState.description,
                        location = formState.location,
                        startDate = formState.startDate,
                        endDate = formState.endDate,
                        // Se removeImage for true, newImageBytes será null (garantido pelo onRemoveImage)
                        newImageBytes = formState.newImageBytes,
                        newImageFileName = if (formState.newImageBytes != null) "update.jpg" else null,
                        removeImage = formState.removeImage
                    )
                }

                // Atualizar as listas globais (para que dropdowns noutros ecrãs fiquem atualizados)
                listsRepository.loadLists()

                _uiState.value = CampaignUiState(success = true)
                closeDialog()
                loadCampaigns() // Atualiza a tabela atual

            } catch (e: Exception) {
                _uiState.value = CampaignUiState(isLoading = false, lastErrorMessage = e.message)
            }
        }
    }

    fun deleteCampaign() {
        // Proteção: não tentar apagar o que ainda não foi criado
        if (formState.id == 0) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CampaignUiState(isLoading = true)
            try {
                campaignRepository.deleteCampaign(formState.id)

                // Atualizar listas globais (remover a campanha dos dropdowns)
                listsRepository.loadLists()

                _uiState.value = CampaignUiState(success = true)
                closeDialog()
                loadCampaigns() // Remove da lista visual

            } catch (e: Exception) {
                _uiState.value = CampaignUiState(isLoading = false, lastErrorMessage = e.message)
            }
        }
    }

    // =========================================================================
    // VALIDAÇÕES E UTILITÁRIOS
    // =========================================================================

    private fun validateField(field: String, value: String) {
        val newErrors = formState.errors.toMutableMap()
        if (field == "name") {
            if (value.isBlank()) newErrors["name"] = "Obrigatório"
            else newErrors.remove("name")
        }
        formState = formState.copy(errors = newErrors)
    }

    private fun validateDates(): Boolean {
        val newErrors = formState.errors.toMutableMap()
        var isValid = true

        if (formState.startDate.isBlank()) {
            newErrors["startDate"] = "Data obrigatória"
            isValid = false
        } else newErrors.remove("startDate")

        if (formState.endDate.isBlank()) {
            newErrors["endDate"] = "Data obrigatória"
            isValid = false
        } else newErrors.remove("endDate")

        if (isValid && formState.startDate > formState.endDate) {
            newErrors["endDate"] = "Data fim inválida"
            isValid = false
        }

        formState = formState.copy(errors = newErrors)
        return isValid
    }

    private fun validateAll(): Boolean {
        validateField("name", formState.name)
        val datesValid = validateDates()
        return formState.errors.isEmpty() && datesValid
    }

    // Helper: Converte dd/MM/yyyy (Input UI) -> yyyy-MM-dd (API)
    private fun convertDateToApi(uiDate: String): String {
        if (uiDate.isBlank()) return ""
        return try {
            val parts = uiDate.split("/")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else uiDate
        } catch (e: Exception) { uiDate }
    }
}