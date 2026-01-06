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
import sasipca.repositories.BeneficiaryRepository
import sasipca.utils.SnackbarManager
import sasipca.models.SnackbarType

data class BeneficiaryUIState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class BeneficiaryDetailViewModel(
    private val beneficiaryRepository: BeneficiaryRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow(BeneficiaryUIState())
    val uiState: StateFlow<BeneficiaryUIState> = _uiState

    var getBeneficiary by mutableStateOf<BeneficiaryGet?>(null)
        private set

    fun clearUiState() {
        _uiState.value = BeneficiaryUIState()
    }

    // --- CARREGAMENTO (Mantido) ---
    fun loadBeneficiary(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                getBeneficiary = beneficiaryRepository.getProfile(id)
            } catch (e: Exception) {
                SnackbarManager.show(e.message ?: "Erro ao carregar.", SnackbarType.ERROR)
            } finally {
                isLoading = false
            }
        }
    }

    fun submitCreateBeneficiary(
        name: String,
        email: String,
        contact: String,
        nifStr: String,
        street: String,
        numberStr: String,
        postalCode: String,
        studentNumStr: String,
        course: String,
        curricularYearStr: String,
        globalObs: String,
        particularObs: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            // 1. Limpar estado anterior
            _uiState.value = BeneficiaryUIState(isLoading = true)
            val errors = mutableMapOf<String, String>()

            // 2. Validações
            if (name.isBlank()) errors["name"] = "O nome é obrigatório."

            if (email.isNotBlank() && !email.contains("@")) errors["email"] = "Email inválido."

            if (email.isBlank()) errors["email"] = "O email é obrigatório."

            // Validação de Contacto (+351...)
            if (contact.isNotBlank()) {
                if (!contact.startsWith("+")) errors["contact"] = "Deve começar com indicativo (ex: +351)"
                else {
                    val digits = contact.filter { it.isDigit() }
                    if (digits.length < 9) errors["contact"] = "Mínimo 9 dígitos."
                    if (digits.length > 13) errors["contact"] = "Máximo 13 dígitos."
                }
            }

            // Validação NIF (9 dígitos)
            val nif = if (nifStr.isNotBlank()) {
                val parsed = nifStr.toIntOrNull()
                if (parsed == null || nifStr.length != 9) {
                    errors["nif"] = "NIF deve ter 9 dígitos numéricos."
                    null
                } else parsed
            } else null

            // Validação Código Postal (xxxx-xxx)
            if (postalCode.isNotBlank()) {
                if (!postalCode.matches(Regex("^\\d{4}-\\d{3}$"))) {
                    errors["postalCode"] = "Formato inválido (ex: 4700-000)."
                }
            }

            // Conversões numéricas simples
            val number = numberStr.toIntOrNull()
            val studentNum = studentNumStr.toIntOrNull()
            val curricularYear = curricularYearStr.toIntOrNull()

            // 3. Verificar Erros
            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errors = errors,
                    lastErrorMessage = "Verifique os erros no formulário."
                )
                return@launch
            }

            // 4. Preparar DTO
            val dto = BeneficiaryPost(
                name = name,
                email = email,
                contact = contact,
                nif = nif,
                street = street,
                number = number,
                postalCode = postalCode,
                studentNum = studentNum,
                course = course,
                curricularYear = curricularYear,
                globalObs = globalObs,
                particularObs = particularObs
            )

            // 5. Enviar para API
            runCatching {
                beneficiaryRepository.postProfile(dto)
            }.onSuccess { _ ->
                _uiState.value = BeneficiaryUIState(success = true)
            }.onFailure { t ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = t.message ?: "Erro ao criar beneficiário."
                )
            }
        }
    }

    fun updateBeneficiary(beneficiaryId: Int, body: BeneficiaryPost) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = _uiState.value.copy(isLoading = true, errors = emptyMap(), lastErrorMessage = null, success = false)

            val errors = mutableMapOf<String, String>()

            // Name
            if (body.name.isEmpty()) errors["name"] = "Nome é obrigatório"

            // Email
            if (body.email.isNotEmpty() && !body.email.contains("@")) errors["email"] = "Email inválido"

            // Contacto
            val contactDigitsOnly = body.contact.replace("[^0-9]".toRegex(), "")
            val length = contactDigitsOnly.length

            if (body.contact.isNotEmpty()) {
                if (!body.contact.startsWith("+")) errors["contact"] = "Deve começar com '+'"
                else if (length < 9) errors["contact"] = "Mínimo 9 dígitos"
                else if (length > 13) errors["contact"] = "Máximo 13 dígitos"
            }

            // NIF
            if (body.nif != null && body.nif < 100000000) { // verificação simples de 9 dígitos
                errors["nif"] = "NIF inválido" // descomentar se necessário
            }

            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errors = errors, lastErrorMessage = "Existem erros no formulário")
                return@launch
            }

            runCatching {
                beneficiaryRepository.putProfile(beneficiaryId, body)
            }.onSuccess { _ ->
                _uiState.value = BeneficiaryUIState(success = true)
            }.onFailure { t ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = t.message ?: "Erro ao atualizar."
                )
            }
        }
    }
}