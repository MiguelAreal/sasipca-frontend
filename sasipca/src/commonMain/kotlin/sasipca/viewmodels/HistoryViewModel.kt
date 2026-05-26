package sasipca.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sasipca.models.*
import sasipca.repositories.HistoryRepository

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var currentTab by mutableStateOf(HistoryTab.MOVEMENTS)
        private set

    // Controla a visibilidade do Dialog
    var isDialogOpen by mutableStateOf(false)
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
                    val movements = repository.getMovements()
                    withContext(Dispatchers.Main) {
                        movementsList = movements
                    }
                } else {
                    val deliveries = repository.getDeliveriesHistory()
                    withContext(Dispatchers.Main) {
                        deliveriesList = deliveries
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun openMovementDetails(id: Int) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detail = repository.getMovementDetails(id)
                withContext(Dispatchers.Main) {
                    selectedMovementDetail = detail
                    isDialogOpen = true // Abre o dialog ao carregar os dados
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun openDeliveryDetails(id: Int) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detail = repository.getDeliveryDetails(id)
                withContext(Dispatchers.Main) {
                    selectedDeliveryDetail = detail
                    isDialogOpen = true // Abre o dialog ao carregar os dados
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun closeDialog() {
        isDialogOpen = false
        selectedMovementDetail = null
        selectedDeliveryDetail = null
    }
}