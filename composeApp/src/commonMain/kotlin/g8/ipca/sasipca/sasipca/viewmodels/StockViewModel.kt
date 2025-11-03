package g8.ipca.sasipca.sasipca.viewmodels

import androidx.compose.runtime.*
import g8.ipca.sasipca.sasipca.models.StockItemDTO
import g8.ipca.sasipca.sasipca.repositories.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StockViewModel(private val repository: StockRepository = StockRepository()) {

    var stockItems by mutableStateOf<List<StockItemDTO>>(emptyList())
        private set

    var filteredItems by mutableStateOf<List<StockItemDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var currentPage by mutableStateOf(1)
        private set

    var pageSize: Int = 10
    var totalPages by mutableStateOf(1)
        private set

    fun loadStock(search: String = searchQuery) {
        searchQuery = search
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allItems = repository.getStock(search)
                stockItems = allItems

                // Paginação manual
                val startIndex = (currentPage - 1) * pageSize
                val pageItems = allItems.drop(startIndex).take(pageSize)
                filteredItems = pageItems

                totalPages = (allItems.size + pageSize - 1) / pageSize
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++
            loadStock(searchQuery)
        }
    }

    fun goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--
            loadStock(searchQuery)
        }
    }
}
