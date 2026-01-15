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
import kotlinx.datetime.number
import sasipca.models.*
import sasipca.repositories.DeliveryRepository
import sasipca.screens.DeliveryProductToSend
import sasipca.utils.updateWidgets
import java.time.LocalDate
import java.time.YearMonth

// Estado da UI para a criação/edição de entregas
data class DeliveryUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val lastErrorMessage: String? = null,
    val success: Boolean = false,
    val successMessage: String? = null
)

class DeliveriesViewModel(private val deliveryRepository: DeliveryRepository) : ViewModel() {

    // --- Estados de Dados ---
    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries

    private val _futureDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val futureDeliveries: StateFlow<List<Delivery>> = _futureDeliveries

    // --- Estados de UI ---
    private val _uiState = MutableStateFlow(DeliveryUiState())
    val uiState: StateFlow<DeliveryUiState> = _uiState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- Métodos de Leitura ---

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
            val start = month.minusMonths(1).atDay(1).toString()
            val end = month.plusMonths(1).atEndOfMonth().toString()

            runCatching {
                deliveryRepository.getDeliveries(DeliveryGet(dateFrom = start, dateTo = end))
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
            }.onSuccess { list ->
                _futureDeliveries.value = list.filter { d -> d.statusId == 1 }.sortedBy { d -> d.scheduledDate }
            }.onFailure { println("Erro: $it") }
            _isLoading.value = false
        }
    }

    suspend fun getDeliveryDetails(id: Int): DeliveryDetail? {
        return try {
            deliveryRepository.getDeliveryDetails(id)
        } catch (e: Exception) {
            println("Erro ao buscar detalhes da entrega $id: ${e.message}")
            null
        }
    }

    fun scheduleDelivery(
        beneficiaryId: Int?,
        scheduledDate: LocalDate?,
        isScheduled: Boolean,
        products: List<DeliveryProductToSend>,
        note: String
    ) {
        submitDeliveryData(
            isUpdate = false,
            deliveryId = 0,
            beneficiaryId = beneficiaryId,
            scheduledDate = scheduledDate,
            isScheduled = isScheduled,
            products = products,
            note = note,
            currentStatusId = 1
        )
    }

    fun updateDelivery(
        deliveryId: Int,
        beneficiaryId: Int?,
        scheduledDate: LocalDate?,
        isScheduled: Boolean,
        products: List<DeliveryProductToSend>,
        note: String,
        currentStatusId: Int
    ) {
        submitDeliveryData(
            isUpdate = true,
            deliveryId = deliveryId,
            beneficiaryId = beneficiaryId,
            scheduledDate = scheduledDate,
            isScheduled = isScheduled,
            products = products,
            note = note,
            currentStatusId = currentStatusId
        )
    }

    private fun submitDeliveryData(
        isUpdate: Boolean,
        deliveryId: Int,
        beneficiaryId: Int?,
        scheduledDate: LocalDate?,
        isScheduled: Boolean,
        products: List<DeliveryProductToSend>,
        note: String,
        currentStatusId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DeliveryUiState(isLoading = true)
            val errors = mutableMapOf<String, String>()

            if (beneficiaryId == null) {
                errors["beneficiary"] = "Selecione um beneficiário."
            }

            val finalDate = if (isScheduled) scheduledDate else LocalDate.now()

            if (finalDate == null) {
                errors["date"] = "Data inválida."
            } else if (isScheduled && finalDate.isBefore(LocalDate.now())) {
                errors["date"] = "A data de agendamento não pode ser no passado."
            }

            if (products.isEmpty()) {
                errors["products"] = "Adicione pelo menos um produto."
            } else {
                val totalItemsCount = products.sumOf { it.quantityToDeliver }
                if (totalItemsCount <= 0) {
                    errors["products"] = "Quantidades devem ser superiores a zero."
                }

                products.forEach { product ->
                    if (product.quantityToDeliver > product.totalStock) {
                        errors["stock_${product.barcode}"] = "Stock insuficiente para ${product.productName}."
                    }

                    product.selectedGroups.forEach { selectedItem ->
                        val groupDetails = product.availableGroups.find { it.id == selectedItem.groupId }
                        val expiryDate = groupDetails?.expiryDate

                        if (expiryDate != null && finalDate != null) {
                            val expJava = LocalDate.of(expiryDate.year, expiryDate.month.number, expiryDate.day)

                            if (expJava.isBefore(finalDate)) {
                                errors["expiry_${product.barcode}"] = "O produto ${product.productName} contém lotes expirados (${expiryDate.day}/${expiryDate.month.number}/${expiryDate.year})."
                            }
                        }
                    }
                }
            }

            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errors = errors,
                    lastErrorMessage = errors.values.firstOrNull() ?: "Verifique os erros no formulário."
                )
                return@launch
            }

            val itemsPayload = products.flatMap { product ->
                product.selectedGroups.map { groupItem ->
                    DeliveryItem(
                        barcode = product.barcode,
                        groupId = groupItem.groupId,
                        quantity = groupItem.quantity
                    )
                }
            }

            try {
                if (isUpdate) {
                    val putBody = DeliveryPut(
                        scheduledDate = finalDate.toString(),
                        note = note.ifBlank { null },
                        itemsToDeliver = itemsPayload,
                        newStatusId = if (!isScheduled) 2 else currentStatusId
                    )

                    val response = deliveryRepository.putDelivery(deliveryId, putBody)

                    _uiState.value = DeliveryUiState(
                        success = true,
                        isLoading = false,
                        successMessage = response.message
                    )
                } else {
                    val postBody = DeliveryPost(
                        beneficiaryId = beneficiaryId!!,
                        scheduledDate = finalDate.toString(),
                        note = note.ifBlank { null },
                        itemsToDeliver = itemsPayload
                    )

                    val response = deliveryRepository.scheduleDelivery(postBody, instant = !isScheduled)

                    _uiState.value = DeliveryUiState(
                        success = true,
                        isLoading = false,
                        successMessage = response.message
                    )
                }
                updateWidgets()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun completeDelivery(deliveryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DeliveryUiState(isLoading = true)
            try {
                // Criamos o DTO apenas com o novo status
                val putBody = DeliveryPut(
                    newStatusId = 2
                )

                val response = deliveryRepository.putDelivery(deliveryId, putBody)

                _uiState.value = DeliveryUiState(
                    success = true,
                    isLoading = false,
                    successMessage = response.message
                )

                loadFutureDeliveries()
                updateWidgets()

            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun deleteDelivery(deliveryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DeliveryUiState(isLoading = true)
            try {
                val response = deliveryRepository.deleteDelivery(deliveryId)
                _uiState.value = DeliveryUiState(
                    success = true,
                    isLoading = false,
                    successMessage = response.message
                )
                updateWidgets()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }



    private suspend fun handleException(e: Exception) {
        val msg = when (e) {
            is ClientRequestException -> try {
                e.response.body<Resposta>().message
            } catch (_: Exception) { e.message }
            is ServerResponseException -> "Erro no servidor. Tente mais tarde."
            else -> e.message ?: "Erro desconhecido."
        }
        _uiState.value = _uiState.value.copy(isLoading = false, lastErrorMessage = msg)
    }

    fun clearUiState() {
        _uiState.value = DeliveryUiState()
    }

    fun selectMonth(m: YearMonth) {
        _month.value = m
        loadMonthDeliveries(m)
    }

}