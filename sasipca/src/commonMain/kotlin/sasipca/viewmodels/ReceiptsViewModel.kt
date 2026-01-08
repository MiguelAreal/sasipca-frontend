package sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.GroupToEnter
import sasipca.models.ReceiptGroupItem
import sasipca.models.ReceiptPost
import sasipca.repositories.ReceiptRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ReceiptUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class ReceiptsViewModel(private val receiptRepository: ReceiptRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState

    private val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Helper para limpar estado após sucesso (Chamado pelo UI)
    fun clearUiState() {
        _uiState.value = ReceiptUiState()
    }

    private fun dateToIsoZeroUtcOrNull(input: String): String? {
        return try {
            val localDate = LocalDate.parse(input, inputDateFormatter)
            localDate.toString() // Retorna yyyy-MM-dd
        } catch (_: Exception) {
            null
        }
    }

    fun submitReceipt(
        barcode: String,
        groupsUi: List<GroupToEnter>,
        name: String?,
        categoryId: Int?,
        unitId: Int?,
        campaignId: Int?,
        unitSizeStr: String,
        note: String?
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = _uiState.value.copy(isLoading = true, errors = emptyMap(), lastErrorMessage = null, success = false)

            val errors = mutableMapOf<String, String>()

            // Validações
            if (barcode.isBlank()) errors["barcode"] = "Código de barras obrigatório"
            if (name.isNullOrBlank()) errors["name"] = "Nome de produto é obrigatório"

            val unitSize = unitSizeStr.toIntOrNull()
            if (unitSize == null) errors["unitSize"] = "Qtd. inválida"
            else if (unitSize < 1) errors["unitSize"] = "Mínimo 1"

            // Validação de Grupos
            val validatedGroups = mutableListOf<ReceiptGroupItem>()
            groupsUi.forEachIndexed { index, l ->
                val prefix = "group_$index"
                var groupValid = true

                // Valida Quantidade
                val qty = l.quantity.toIntOrNull()
                if (l.quantity.isBlank()) {
                    errors["$prefix.quantity"] = "Obrigatório"
                    groupValid = false
                } else if (qty == null || qty <= 0) {
                    errors["$prefix.quantity"] = "> 0"
                    groupValid = false
                }

                // Valida Data
                if (l.expiryDate.isBlank()) {
                    errors["$prefix.expiryDate"] = "Obrigatória"
                } else {
                    val iso = dateToIsoZeroUtcOrNull(l.expiryDate)
                    if (iso == null) {
                        errors["$prefix.expiryDate"] = "Inválida"
                    } else if (groupValid) {
                        // Só adiciona se tudo estiver válido
                        validatedGroups.add(ReceiptGroupItem(quantity = qty!!, expiryDate = iso))
                    }
                }
            }

            if (validatedGroups.isEmpty()) {
                errors["groups"] = "Adicione pelo menos um lote válido"
            }

            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errors = errors, lastErrorMessage = "Verifique os erros no formulário.")
                return@launch
            }

            // Construir Body
            val body = ReceiptPost(
                barcode = barcode,
                groups = validatedGroups,
                name = name,
                categoryId = categoryId,
                unitId = unitId,
                campaignId = campaignId,
                unitSize = unitSize!!,
                note = note?.takeIf { it.isNotBlank() }
            )

            // Enviar
            runCatching {
                receiptRepository.postReceipt(body)
            }.onSuccess {
                _uiState.value = ReceiptUiState(success = true)
            }.onFailure { t ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = t.message ?: "Erro ao submeter receção."
                )
            }
        }
    }
}