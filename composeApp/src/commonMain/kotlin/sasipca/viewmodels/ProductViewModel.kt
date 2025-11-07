package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import sasipca.models.ProductItemDTO
import sasipca.repositories.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    var stockItems by mutableStateOf<List<ProductItemDTO>>(emptyList())
        private set

    var filteredItems by mutableStateOf<List<ProductItemDTO>>(emptyList())
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

    fun loadProducts(search: String = searchQuery) {
        searchQuery = search
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getProducts(search)

                // Extrai os items do PaginatedResponse
                val allItems = response.data

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
            loadProducts(searchQuery)
        }
    }

    fun goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--
            loadProducts(searchQuery)
        }
    }
}
