package sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.Delivery
import sasipca.models.DeliveryGet
import sasipca.models.DeliveryPost
import sasipca.models.DeliveryPut
import sasipca.repositories.DeliveryRepository
import java.time.LocalDate
import java.time.YearMonth

class DeliveriesViewModel(private val deliveryRepository: DeliveryRepository) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Busca todas as entregas feitas a um beneficiário.
     *
     * Usado pelo beneficiary profile.
     */
    fun loadBeneficiaryDeliveries(beneficiaryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                deliveryRepository.getDeliveries(
                    DeliveryGet(
                        beneficiaryId = beneficiaryId
                    )
                )
            }.onSuccess {
                _deliveries.value = it
            }.onFailure {
                println("Erro ao carregar entregas do beneficiário: $it")
            }
            _isLoading.value = false
        }
    }

    /**
     * Busca todas as entregas planeadas para um período de meses específico.
     *
     * Usado pelo calendar widget.
     */
    fun loadMonthDeliveries(month: YearMonth = _month.value) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                deliveryRepository.getDeliveries(
                    DeliveryGet(
                        dateFrom = month.atDay(1).toString(),
                        dateTo = month.atEndOfMonth().toString()
                    )
                )
            }.onSuccess {
                _deliveries.value = it
            }.onFailure {
                println("Erro ao carregar entregas: $it")
            }
            _isLoading.value = false
        }
    }


    /**
     * Agenda nova entrega (instant = false)
     *
     * Executa nova entrega imediatamente (instant = true)
     */
    fun scheduleDelivery(body: DeliveryPost, instant: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                deliveryRepository.scheduleDelivery(body,instant)
            }.onSuccess {
                loadMonthDeliveries(_month.value)
            }
        }
    }

    /**
     * Update a entrega agendada existente.
     */
    fun updateDelivery(deliveryId: Int, dto: DeliveryPut) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                deliveryRepository.putDelivery(deliveryId, dto)
            }.onSuccess {
                loadMonthDeliveries(_month.value)
            }
        }
    }

    fun selectMonth(newMonth: YearMonth) {
        _month.value = newMonth
        loadMonthDeliveries(newMonth)
    }

    fun selectDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }
}
