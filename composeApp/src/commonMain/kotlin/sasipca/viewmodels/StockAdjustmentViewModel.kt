package sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.ProductGroup
import sasipca.models.StockAdjustment
import sasipca.repositories.AdjustmentRepository

data class StockAdjustmentUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class StockAdjustmentViewModel(private val repository: AdjustmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StockAdjustmentUiState())
    val uiState: StateFlow<StockAdjustmentUiState> = _uiState

    fun submitAdjustment(
        barcode: String,
        selectedGroup: ProductGroup?,
        quantityStr: String,
        isAddition: Boolean,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = StockAdjustmentUiState(isLoading = true)

            // 1. Validações
            if (barcode.isBlank() || selectedGroup == null) {
                _uiState.value = StockAdjustmentUiState(errorMessage = "Selecione um produto e um lote.")
                return@launch
            }

            val qty = quantityStr.toIntOrNull()
            if (qty == null || qty <= 0) {
                _uiState.value = StockAdjustmentUiState(errorMessage = "Quantidade inválida.")
                return@launch
            }

            if (note.isBlank()) {
                _uiState.value = StockAdjustmentUiState(errorMessage = "A justificação é obrigatória.")
                return@launch
            }

            // Validar Saída vs Stock Disponível
            if (!isAddition && qty > selectedGroup.availableStock) {
                _uiState.value = StockAdjustmentUiState(errorMessage = "Não pode remover mais do que o stock disponível (${selectedGroup.availableStock}).")
                return@launch
            }

            // 2. Preparar DTO (Inverter sinal se for saída)
            val finalQuantity = if (isAddition) qty else -qty

            val dto = StockAdjustment(
                barcode = barcode,
                groupId = selectedGroup.id,
                quantityAdjustment = finalQuantity,
                note = note
            )

            // 3. Enviar
            try {
                val response = repository.adjustStock(dto)
                _uiState.value = StockAdjustmentUiState(success = true, successMessage = response.message)
            } catch (e: Exception) {
                _uiState.value = StockAdjustmentUiState(errorMessage = e.message ?: "Erro ao ajustar stock.")
            }
        }
    }

    fun clearState() {
        _uiState.value = StockAdjustmentUiState()
    }
}