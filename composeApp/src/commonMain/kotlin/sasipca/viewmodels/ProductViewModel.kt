package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import sasipca.repositories.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.models.ProductItem
import sasipca.models.ProductItemUI
import sasipca.storage.ListsStore

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    var stockItems by mutableStateOf<List<ProductItemUI>>(emptyList())
        private set

    var filteredItems by mutableStateOf<List<ProductItemUI>>(emptyList())
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

    private fun mapToUI(dto: ProductItem): ProductItemUI {
        return ProductItemUI(
            barcode = dto.barcode,
            name = dto.name,
            categoryName = ListsStore.getCategoryName(dto.categoryId),
            unitName = ListsStore.getUnitTypeName(dto.unitId),
            unitSize = dto.unitSize,
            totalQuantity = dto.totalQuantity,
            reservedQuantity = dto.reservedQuantity,
            availableStock = dto.availableStock
        )
    }

    fun loadProducts(search: String = searchQuery) {
        searchQuery = search
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getProducts(search)

                // Extrai os items do PaginatedResponse
                val rawItems = response.data

                val uiItems = rawItems.map { mapToUI(it) }

                stockItems = uiItems

                // Paginação manual
                val startIndex = (currentPage - 1) * pageSize
                val pageItems = uiItems.drop(startIndex).take(pageSize)
                filteredItems = pageItems

                totalPages = (uiItems.size + pageSize - 1) / pageSize
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
