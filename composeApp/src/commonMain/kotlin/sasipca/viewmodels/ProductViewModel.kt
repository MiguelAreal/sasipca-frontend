package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import sasipca.repositories.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.models.ProductDetailUI
import sasipca.models.ProductUI
import sasipca.repositories.OFFRepository
import sasipca.storage.ListsStore

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var stockItems by mutableStateOf<List<ProductUI>>(emptyList())
        private set

    var filteredItems by mutableStateOf<List<ProductUI>>(emptyList())
        private set

    var selectedProductDetail by mutableStateOf<ProductDetailUI?>(null)
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

    /**
     * Deseleciona um produto
     */
    fun resetProduct() {
        selectedProductDetail = null
    }

    /**
     * Carrega produto específico da nossa API.
     */
    fun loadProduct(barcode: String) {
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val detail = repository.getProduct(barcode)

                val uiDetail = ProductDetailUI(
                    barcode = detail.barcode,
                    name = detail.name,
                    unitSize = detail.unitSize,
                    categoryName = ListsStore.getCategoryTypeName(detail.categoryId),
                    unitName = ListsStore.getUnitTypeName(detail.unitId),
                    totalQuantity = detail.totalQuantity,
                    reservedQuantity = detail.reservedQuantity,
                    availableStock = detail.availableStock,
                    productLots = detail.productLots
                )

                selectedProductDetail = uiDetail
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Através do código de barras carrega um produto.
     * - Se o produto existir na nossa base de dados, carrega primeiro esses dados:
     *      - Nome de produto
     *      - Categoria
     *      - Tipo de unidade
     *      - Quantidade unitária
     * - Se não existir connosco, vem tudo do OpenFoodFacts.
     *
     * As imagens vêm sempre do OpenFoodFacts.
     *
     */
    fun loadProductHybrid(barcode: String, offRepository: OFFRepository) {
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1️⃣ Tenta buscar na API interna
                val detail = try { repository.getProduct(barcode) } catch (_: Exception) { null }

                // 2️⃣ Busca no Open Food Facts
                val offResponse = try { offRepository.getProduct(barcode) } catch (_: Exception) { null }
                val offProduct = offResponse?.product

                val uiDetail = when {
                    detail != null -> {
                        // Produto existe na API interna → API é fonte de verdade para dados
                        ProductDetailUI(
                            barcode = detail.barcode,
                            name = detail.name,
                            unitSize = detail.unitSize,
                            categoryName = ListsStore.getCategoryTypeName(detail.categoryId),
                            unitName = ListsStore.getUnitTypeName(detail.unitId),
                            totalQuantity = detail.totalQuantity,
                            reservedQuantity = detail.reservedQuantity,
                            availableStock = detail.availableStock,
                            productLots = detail.productLots
                        ).also {
                            // Imagens sempre do OFF se disponíveis
                            it.images = offProduct?.images ?: emptyList()
                        }
                    }
                    offProduct != null -> {
                        // Produto novo → tudo vem do OFF
                        ProductDetailUI(
                            barcode = offResponse.code,
                            name = offProduct.product_name ?: "",
                            unitSize = offProduct.product_quantity,
                            categoryName = "", // usuário preenche depois
                            unitName = offProduct.product_quantity_unit ?: "",
                            totalQuantity = null,
                            reservedQuantity = null,
                            availableStock = null,
                            productLots = emptyList()
                        ).also {
                            it.images = offProduct.images
                        }
                    }
                    else -> null
                }

                selectedProductDetail = uiDetail

            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }



    /**
     * Carrega lista paginada de produtos
     */
    fun loadProducts(search: String = searchQuery) {
        searchQuery = search
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getProducts(search)
                val uiItems = response.data.map { dto ->
                    ProductUI(
                        barcode = dto.barcode,
                        name = dto.name,
                        categoryName = ListsStore.getCategoryTypeName(dto.categoryId),
                        unitName = ListsStore.getUnitTypeName(dto.unitId),
                        unitSize = dto.unitSize,
                        totalQuantity = dto.totalQuantity,
                        reservedQuantity = dto.reservedQuantity,
                        availableStock = dto.availableStock
                    )
                }

                stockItems = uiItems

                val startIndex = (currentPage - 1) * pageSize
                filteredItems = uiItems.drop(startIndex).take(pageSize)

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
