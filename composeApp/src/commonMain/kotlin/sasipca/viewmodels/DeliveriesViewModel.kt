package sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sasipca.repositories.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.DeliveryCreationDTO
import sasipca.models.DeliveryQueryDTO
import sasipca.models.DeliveryUpdateDTO
import sasipca.models.VDeliveryDTO
import java.time.LocalDate
import java.time.YearMonth

class DeliveriesViewModel(private val stockRepository: StockRepository) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _deliveries = MutableStateFlow<List<VDeliveryDTO>>(emptyList())
    val deliveries: StateFlow<List<VDeliveryDTO>> = _deliveries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Get all deliveries done to a specific beneficiary.
     * Used for beneficiary profile.
     */
    fun loadBeneficiaryDeliveries(beneficiaryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                stockRepository.getDeliveries(
                    DeliveryQueryDTO(
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
     * Get all deliveries planned for a specific month time-period.
     * Used by calendar widget.
     */
    fun loadMonthDeliveries(month: YearMonth = _month.value) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                stockRepository.getDeliveries(
                    DeliveryQueryDTO(
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
     * Schedule a new delivery.
     */
    fun scheduleDelivery(dto: DeliveryCreationDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                stockRepository.scheduleDelivery(dto)
            }.onSuccess {
                loadMonthDeliveries(_month.value)
            }
        }
    }

    /**
     * Update an existing delivery.
     */
    fun updateDelivery(deliveryId: Int, dto: DeliveryUpdateDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                stockRepository.updateDelivery(deliveryId, dto)
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
