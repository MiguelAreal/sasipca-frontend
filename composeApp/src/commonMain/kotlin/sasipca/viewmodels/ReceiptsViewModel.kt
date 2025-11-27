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
import sasipca.navigation.NavigationService
import sasipca.repositories.ReceiptRepository
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class ReceiptUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(), // chave -> mensagem (ex: "barcode" -> "Obrigatório")
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class ReceiptsViewModel(private val receiptRepository: ReceiptRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState

    private val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val outputInstantFormatter = DateTimeFormatter.ISO_INSTANT

    private fun dateToIsoZeroUtcOrNull(input: String): String? {
        return try {
            val localDate = LocalDate.parse(input, inputDateFormatter)
            val zoned = localDate.atStartOfDay(ZoneOffset.UTC)
            outputInstantFormatter.format(zoned.toInstant())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Valida e submete a receção.
     * Parâmetros: valores exatamente tal como vêm do UI (strings para campos editáveis).
     */
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
            // inicia limpeza de estado
            _uiState.value = _uiState.value.copy(isLoading = false, errors = emptyMap(), lastErrorMessage = null, success = false)

            val errors = mutableMapOf<String, String>()

            // barcode
            if (barcode.isBlank()) {
                errors["barcode"] = "Código de barras obrigatório"
            }

            name?.let {
                if (it.isBlank()) {
                    errors["name"] = "Nome de produto é obrigatório"
                }
            }

            // unitSize
            val unitSize = unitSizeStr.toIntOrNull()
            if (unitSize == null) {
                errors["unitSize"] = "Quantidade por unidade inválida"
            } else if (unitSize <= 1) {
                errors["unitSize"] = "Quantidade por unidade tem de ser maior que 1"
            }

            // validar grupos individuais
            val validatedGroups = mutableListOf<ReceiptGroupItem>()
            groupsUi.forEachIndexed { index, l ->
                val prefix = "group_$index"
                if (l.quantity.isBlank()) {
                    errors["$prefix.quantity"] = "Quantidade obrigatória"
                } else {
                    val qty = l.quantity.toIntOrNull()
                    if (qty == null) {
                        errors["$prefix.quantity"] = "Quantidade inválida"
                    } else if (qty <= 0) {
                        errors["$prefix.quantity"] = "Quantidade deve ser maior que zero"
                    }
                }

                if (l.expiryDate.isBlank()) {
                    errors["$prefix.expiryDate"] = "Data de validade obrigatória"
                } else {
                    val iso = dateToIsoZeroUtcOrNull(l.expiryDate)
                    if (iso == null) {
                        errors["$prefix.expiryDate"] = "Data inválida (use dd/MM/yyyy)"
                    } else {
                        // Se chegámos até aqui, quantity já foi validado
                        val qty = l.quantity.toIntOrNull()
                        if (qty != null && qty > 0) {
                            validatedGroups.add(ReceiptGroupItem(quantity = qty, expiryDate = iso))
                        }
                    }
                }
            }

            if (validatedGroups.isEmpty()) {
                errors["groups"] = "Pelo menos um grupo válido é necessário"
            }

            // se existirem erros, atualiza estado e sai
            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errors = errors, lastErrorMessage = "Existem erros no formulário")
                return@launch
            }

            // construir body
            val body = ReceiptPost(
                barcode = barcode,
                groups = validatedGroups,
                name = name?.takeIf { it.isNotBlank() },
                categoryId = categoryId,
                unitId = unitId,
                campaignId = campaignId,
                unitSize = unitSize!!,
                note = note?.takeIf { it.isNotBlank() }
            )

            // enviar
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                receiptRepository.postReceipt(body)
            }.onSuccess { response ->
                // considera sucesso — volta atrás na navegação
                _uiState.value = ReceiptUiState(success = true)
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
