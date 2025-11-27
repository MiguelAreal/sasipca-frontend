package sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.Delivery
import sasipca.models.DeliveryGet
import sasipca.models.DeliveryItem
import sasipca.models.DeliveryPost
import sasipca.models.Resposta
import sasipca.navigation.NavigationService
import sasipca.repositories.DeliveryRepository
import sasipca.screens.DeliveryProductToSend
import java.time.LocalDate
import java.time.YearMonth

// Estado da UI para a criação de entregas
data class DeliveryUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val lastErrorMessage: String? = null,
    val success: Boolean = false,
    val successMessage: String? = null // NOVO: Mensagem vinda da API
)

class DeliveriesViewModel(private val deliveryRepository: DeliveryRepository) : ViewModel() {

    // --- Estados existentes ---
    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries

    private val _futureDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val futureDeliveries: StateFlow<List<Delivery>> = _futureDeliveries

    // --- Novo Estado de UI ---
    private val _uiState = MutableStateFlow(DeliveryUiState())
    val uiState: StateFlow<DeliveryUiState> = _uiState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- Métodos de Leitura (Mantidos) ---
    fun loadBeneficiaryDeliveries(beneficiaryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                deliveryRepository.getDeliveries(DeliveryGet(beneficiaryId = beneficiaryId))
            }.onSuccess { _deliveries.value = it }
                .onFailure { println("Erro: $it") }
            _isLoading.value = false
        }
    }

    fun loadMonthDeliveries(month: YearMonth = _month.value) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                deliveryRepository.getDeliveries(DeliveryGet(dateFrom = month.atDay(1).toString(), dateTo = month.atEndOfMonth().toString()))
            }.onSuccess { _deliveries.value = it }
                .onFailure { println("Erro: $it") }
            _isLoading.value = false
        }
    }

    fun loadFutureDeliveries() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                deliveryRepository.getDeliveries(DeliveryGet(dateFrom = LocalDate.now().toString()))
            }.onSuccess {
                _futureDeliveries.value = it.filter { d -> d.statusId == 1 }.sortedBy { d -> d.scheduledDate }
            }.onFailure { println("Erro: $it") }
            _isLoading.value = false
        }
    }

    /**
     * Valida e submete uma nova entrega.
     */
    fun scheduleDelivery(
        beneficiaryId: Int?,
        scheduledDate: LocalDate?,
        isScheduled: Boolean,
        products: List<DeliveryProductToSend>,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Limpar estado anterior
            _uiState.value = DeliveryUiState(isLoading = true)
            val errors = mutableMapOf<String, String>()

            // 2. Validações Frontend
            if (beneficiaryId == null) errors["beneficiary"] = "Selecione um beneficiário."

            val finalDate = if (isScheduled) scheduledDate else LocalDate.now()
            if (finalDate == null) errors["date"] = "Data inválida."
            else if (isScheduled && finalDate.isBefore(LocalDate.now())) errors["date"] = "Data não pode ser no passado."

            if (products.isEmpty()) {
                errors["products"] = "Adicione pelo menos um produto."
            } else {
                val totalItemsCount = products.sumOf { it.selectedGroups.sumOf { group -> group.quantity } }
                if (totalItemsCount <= 0) errors["products"] = "Quantidades devem ser superiores a zero."

                products.forEach { product ->
                    if (product.quantityToDeliver > product.totalStock) {
                        errors["stock_${product.barcode}"] = "Stock insuficiente para ${product.productName}."
                    }
                }
            }

            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errors = errors,
                    lastErrorMessage = errors.values.firstOrNull() ?: "Verifique os erros."
                )
                return@launch
            }

            // 3. Preparação dos dados
            val itemsToDeliverPayload = products.flatMap { product ->
                product.selectedGroups.map { groupItem ->
                    DeliveryItem(barcode = product.barcode, groupId = groupItem.groupId, quantity = groupItem.quantity)
                }
            }

            val deliveryPost = DeliveryPost(
                beneficiaryId = beneficiaryId!!,
                scheduledDate = finalDate.toString(),
                note = if (note.isBlank()) null else note,
                itemsToDeliver = itemsToDeliverPayload
            )

            // 4. Chamada à API com Tratamento de Erro Robusto
            try {
                val response = deliveryRepository.scheduleDelivery(deliveryPost, instant = !isScheduled)

                // SUCESSO: Usa a mensagem da API
                _uiState.value = DeliveryUiState(
                    success = true,
                    isLoading = false,
                    successMessage = response.message
                )
                NavigationService.goBack()

            } catch (e: ClientRequestException) {
                // ERRO 4xx: Tenta ler o JSON de erro da API (ex: {"message": "Stock insuficiente"})
                val errorMsg = try {
                    val errorBody = e.response.body<Resposta>()
                    errorBody.message
                } catch (parseEx: Exception) {
                    // Se não conseguir ler o JSON, usa o texto padrão do erro
                    e.message ?: "Erro de validação no servidor."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = errorMsg
                )

            } catch (e: ServerResponseException) {
                // ERRO 5xx
                _uiState.value = _uiState.value.copy(isLoading = false, lastErrorMessage = "Erro no servidor. Tente mais tarde.")
            } catch (e: Exception) {
                // OUTROS ERROS (Sem internet, etc)
                _uiState.value = _uiState.value.copy(isLoading = false, lastErrorMessage = e.message ?: "Erro desconhecido.")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = DeliveryUiState()
    }

    // --- Helpers ---
    fun selectMonth(newMonth: YearMonth) {
        _month.value = newMonth
        loadMonthDeliveries(newMonth)
    }

    fun selectDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }
}