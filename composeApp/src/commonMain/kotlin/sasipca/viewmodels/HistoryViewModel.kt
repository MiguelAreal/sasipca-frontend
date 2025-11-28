package sasipca.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.models.*
import sasipca.repositories.HistoryRepository

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var currentTab by mutableStateOf(HistoryTab.MOVEMENTS)
        private set

    // Listas
    var movementsList by mutableStateOf<List<MovementHistory>>(emptyList())
        private set
    var deliveriesList by mutableStateOf<List<DeliveryHistory>>(emptyList())
        private set

    // Detalhes (para o Dialog)
    var selectedMovementDetail by mutableStateOf<MovementDetail?>(null)
        private set
    var selectedDeliveryDetail by mutableStateOf<DeliveryDetail?>(null)
        private set

    init {
        loadData()
    }

    fun switchTab(tab: HistoryTab) {
        currentTab = tab
        loadData()
    }

    fun loadData() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentTab == HistoryTab.MOVEMENTS) {
                    movementsList = repository.getMovements()
                } else {
                    deliveriesList = repository.getDeliveriesHistory()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun openMovementDetails(id: Int) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                selectedMovementDetail = repository.getMovementDetails(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun openDeliveryDetails(id: Int) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                selectedDeliveryDetail = repository.getDeliveryDetails(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun closeDialog() {
        selectedMovementDetail = null
        selectedDeliveryDetail = null
    }
}