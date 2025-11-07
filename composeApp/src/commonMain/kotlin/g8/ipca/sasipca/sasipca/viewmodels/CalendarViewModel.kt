package g8.ipca.sasipca.sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import g8.ipca.sasipca.sasipca.models.*
import g8.ipca.sasipca.sasipca.repositories.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(private val stockRepository: StockRepository) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _deliveries = MutableStateFlow<List<VDeliveryDTO>>(emptyList())
    val deliveries: StateFlow<List<VDeliveryDTO>> = _deliveries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadDeliveries(month: YearMonth = _month.value) {
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


    fun scheduleDelivery(dto: DeliveryCreationDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                stockRepository.scheduleDelivery(dto)
            }.onSuccess {
                loadDeliveries(_month.value)
            }
        }
    }

    fun updateDelivery(deliveryId: Int, dto: DeliveryUpdateDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                stockRepository.updateDelivery(deliveryId, dto)
            }.onSuccess {
                loadDeliveries(_month.value)
            }
        }
    }

    fun selectMonth(newMonth: YearMonth) {
        _month.value = newMonth
        loadDeliveries(newMonth)
    }

    fun selectDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }
}
