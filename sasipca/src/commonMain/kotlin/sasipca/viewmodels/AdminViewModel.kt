package sasipca.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.repositories.AdminRepository
import sasipca.models.AdminUser

// Estado para feedback de ações (Criar/Editar)
data class AdminUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(), // chave -> mensagem (ex: "email" -> "Inválido")
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class AdminViewModel(private val repository: AdminRepository) : ViewModel() {

    // --- ESTADOS DE UI (Feedback de Ações) ---
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    // --- ESTADOS DE LISTAGEM ---
    var admins by mutableStateOf<List<AdminUser>>(emptyList())
        private set

    var isLoadingList by mutableStateOf(false)
        private set

    var errorMessageList by mutableStateOf<String?>(null)
        private set

    // --- PAGINAÇÃO E PESQUISA ---
    var searchQuery by mutableStateOf("")
        private set

    var currentPage by mutableStateOf(1)
        private set

    var totalPages by mutableStateOf(1)
        private set

    private val pageSize = 10

    /**
     * Limpa o estado de sucesso/erro para permitir novas ações
     */
    fun clearUiState() {
        _uiState.value = AdminUiState()
    }

    /**
     * Carrega lista paginada de administradores
     */
    fun loadAdmins(query: String = searchQuery, page: Int = currentPage) {
        // Se mudarmos a pesquisa, resetamos para página 1, senão usamos a página pedida
        val targetPage = if (query != searchQuery) 1 else page

        searchQuery = query
        currentPage = targetPage

        isLoadingList = true
        errorMessageList = null

        // Usamos IO para operações de rede
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getAdmins(
                    page = currentPage,
                    pageSize = pageSize,
                    search = searchQuery
                )

                if (response != null) {
                    admins = response.data
                    totalPages = response.totalPages
                    // Sincroniza caso o servidor devolva uma página diferente (ex: fora dos limites)
                    if (response.pageNumber > 0) currentPage = response.pageNumber
                } else {
                    errorMessageList = "Erro ao carregar lista de administradores."
                }
            } catch (e: Exception) {
                errorMessageList = "Erro de conexão: ${e.message}"
            } finally {
                isLoadingList = false
            }
        }
    }

    /**
     * Valida e Cria um novo Administrador
     */
    fun createAdmin(email: String, contact: String) {
        viewModelScope.launch(Dispatchers.Default) {
            // 1. Limpeza inicial de estado
            _uiState.value = AdminUiState(isLoading = false) // Limpa erros anteriores

            val errors = mutableMapOf<String, String>()

            // 2. Validações Locais
            if (email.isBlank()) {
                errors["email"] = "O email é obrigatório."
            } else if (!email.endsWith("ipca.pt")) {
                errors["email"] = "O email deve ser do domínio ipca.pt."
            }

            if (contact.isBlank()) {
                errors["contact"] = "O contacto é obrigatório."
            }

            // Se existirem erros, atualiza estado e aborta
            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errors = errors,
                    lastErrorMessage = "Por favor, verifique os campos assinalados."
                )
                return@launch
            }

            // 3. Submissão
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching {
                repository.createAdmin(email, contact)
            }.onSuccess { _ ->
                _uiState.value = AdminUiState(success = true)
                loadAdmins()
            }.onFailure { t ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = t.message ?: "Erro ao criar administrador."
                )
            }
        }
    }

    /**
     * Navegação de Páginas
     */
    fun goToNextPage() {
        if (currentPage < totalPages) {
            loadAdmins(page = currentPage + 1)
        }
    }

    fun goToPreviousPage() {
        if (currentPage > 1) {
            loadAdmins(page = currentPage - 1)
        }
    }
}