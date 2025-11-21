package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.BeneficiaryGet
import sasipca.models.BeneficiaryPost
import sasipca.models.ProductPut
import sasipca.models.Resposta
import sasipca.navigation.NavigationService
import sasipca.repositories.BeneficiaryRepository
import sasipca.utils.SnackbarManager
import sasipca.utils.SnackbarType

data class BeneficiaryUIState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(), // chave -> mensagem (ex: "barcode" -> "Obrigatório")
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

/**
 * ViewModel responsável por gerir o estado de um beneficiário específico.
 */
class BeneficiaryDetailViewModel(
    private val beneficiaryRepository: BeneficiaryRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow(BeneficiaryUIState())

    val uiState: StateFlow<BeneficiaryUIState> = _uiState

    var getBeneficiary by mutableStateOf<BeneficiaryGet?>(null)
        private set


    /**
     * Carrega um beneficiário existente pelo ID.
     */
    fun loadBeneficiary(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                getBeneficiary = beneficiaryRepository.getProfile(id)
            } catch (e: Exception) {
                SnackbarManager.show(
                    message = e.message ?: "Erro ao carregar beneficiário.",
                    type = SnackbarType.ERROR
                )
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Cria um novo beneficiário.
     */
    fun createBeneficiary(dto: BeneficiaryPost, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                val response: Resposta = beneficiaryRepository.postProfile(dto)
                SnackbarManager.show(
                    message = response.message ?: "Beneficiário criado com sucesso.",
                    type = SnackbarType.SUCCESS
                )
                onSuccess?.invoke()
            } catch (e: Exception) {
                SnackbarManager.show(
                    message = e.message ?: "Erro ao criar beneficiário.",
                    type = SnackbarType.ERROR
                )
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Atualiza um beneficiário existente.
     */
    fun updateBeneficiary(beneficiaryId: Int, body: BeneficiaryPost) {
        viewModelScope.launch(Dispatchers.Default) {
            // inicia limpeza de estado
            _uiState.value = _uiState.value.copy(isLoading = false, errors = emptyMap(), lastErrorMessage = null, success = false)

            val errors = mutableMapOf<String, String>()

            // Name (Tem de ter valor)
            if (body.name.isEmpty()) {
                errors["name"] = "Nome é obrigatório"
            }

            // Email (Tem de ter valor e @))
            if(body.email.isEmpty()) errors["email"] = "Email não pode estar vazio"
            if (!body.email.contains("@"))errors["email"] = "Email tem de ter '@'"

            // Contacto
            // Validação de Indicativo (deve começar com '+')
            // Validação de Comprimento Mínimo (9 dígitos numéricos no total)
            // Validação de Comprimento Máximo (13 dígitos numéricos no total)
            // Remove todos os caracteres que não são dígitos (0-9) para obter o comprimento numérico.
            val contactDigitsOnly = body.contact.replace("[^0-9]".toRegex(), "")
            val length = contactDigitsOnly.length

            if (!body.contact.startsWith("+")) {
                errors["contact"] = "O Contacto tem de começar com o indicativo internacional (ex: +351)."
            }
            else if (length < 9) {
                errors["contact"] = "O Contacto tem de ter no mínimo 9 dígitos."
            }
            else if (length > 13) {
                errors["contact"] = "O Contacto tem de ter no máximo 13 dígitos."
            }


            // NIF (tem de ser 9)
            if (body.nif != null && body.nif < 9) {
                errors["nif"] = "NIF tem de ter 9 dígitos."
            }

            // PostalCode (tem de ser válido no formato xxxx-xxx)

            if (body.postalCode != null && body.postalCode.isNotEmpty()) {
                val postalCodeRegex = Regex("^\\d{4}-\\d{3}$")

                if (!body.postalCode.matches(postalCodeRegex)) {
                    errors["postalCode"] = "O Código postal tem de estar no formato xxxx-xxx (ex: 4700-000)."
                }
            }


            // se existirem erros, atualiza estado e sai
            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errors = errors, lastErrorMessage = "Existem erros no formulário")
                return@launch
            }

            // enviar
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                beneficiaryRepository.putProfile(beneficiaryId,body)
            }.onSuccess { response ->
                // considera sucesso — volta atrás na navegação
                _uiState.value = BeneficiaryUIState(success = true)

                SnackbarManager.show(
                    message = response.message ?: "Beneficiário atualizado com sucesso.",
                    type = SnackbarType.SUCCESS
                )
                // navigation
                NavigationService.goBack()
            }.onFailure { t ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = t.message ?: "Erro ao submeter"
                )
            }
        }

    }
}
