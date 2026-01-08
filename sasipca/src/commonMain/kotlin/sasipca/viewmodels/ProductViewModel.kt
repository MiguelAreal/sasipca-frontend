package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sasipca.repositories.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.Product
import sasipca.models.ProductDetail
import sasipca.models.ProductPut
import sasipca.models.UnitTypeInfo
import sasipca.repositories.OFFRepository
import sasipca.storage.ListsStore

data class ProductUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(), // chave → mensagem (ex: "barcode" ⇾ "Obrigatório")
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    // Lista completa (resultado da pesquisa)
    var stockItems by mutableStateOf<List<Product>>(emptyList())
        private set

    // Lista paginada e filtrada para a UI
    var filteredItems by mutableStateOf<List<Product>>(emptyList())
        private set

    var selectedProductDetail by mutableStateOf<ProductDetail?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Estado para filtro de Categoria
    var selectedCategoryId by mutableStateOf<Int?>(null)
        private set

    var currentPage by mutableIntStateOf(1)
        private set

    var pageSize: Int = 10
    var totalPages by mutableIntStateOf(1)
        private set

    /**
     * Atualiza a categoria selecionada e recarrega a lista
     */
    fun onCategoryChange(categoryId: Int?) {
        selectedCategoryId = categoryId
        currentPage = 1 // Reset paginação ao mudar filtro
        loadProducts(searchQuery)
    }

    /**
     * Desseleciona um produto
     */
    fun resetProduct() {
        selectedProductDetail = null
    }

    /**
     * Carrega produto específico da nossa API.
     * Carrega imagens do OpenFoodFacts, caso existam.
     */
    fun getProduct(barcode: String, offRepository: OFFRepository) {
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Busca sempre o produto na API interna (se falhar, é erro)
                val detail = productRepository.getProduct(barcode)

                // Busca imagens no OFF (se falhar, ignora)
                val offResponse = try { offRepository.getProduct(barcode) } catch (_: Exception) { null }
                val offImages = offResponse?.product?.images ?: emptyList()

                // Constrói o objeto de UI usando APENAS a API como fonte de verdade
                val uiDetail = ProductDetail(
                    barcode = detail.barcode,
                    name = detail.name,
                    unitSize = detail.unitSize,
                    categoryId = detail.categoryId,
                    unitId = detail.unitId,
                    totalQuantity = detail.totalQuantity,
                    reservedQuantity = detail.reservedQuantity,
                    availableStock = detail.availableStock,
                    expNotif = detail.expNotif,
                    productGroups = detail.productGroups
                ).also {
                    it.images = offImages
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
     * Através do código de barras carrega um produto.
     * Isto serve para receção de produtos
     * - Se o produto existir na nossa base de dados, carrega primeiro esses dados:
     * - Nome de produto
     * - Categoria
     * - Tipo de unidade
     * - Quantidade unitária
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
                // Tenta buscar na API interna
                val detail = try { productRepository.getProduct(barcode) } catch (_: Exception) { null }

                // Busca no Open Food Facts
                val offResponse = try { offRepository.getProduct(barcode) } catch (_: Exception) { null }
                val offProduct = offResponse?.product

                val uiDetail = when {
                    detail != null -> {
                        // Produto existe na API interna → API é fonte de verdade para dados
                        ProductDetail(
                            barcode = detail.barcode,
                            name = detail.name,
                            unitSize = detail.unitSize,
                            categoryId = detail.categoryId,
                            unitId = detail.unitId,
                            totalQuantity = detail.totalQuantity,
                            reservedQuantity = detail.reservedQuantity,
                            availableStock = detail.availableStock,
                            expNotif = detail.expNotif,
                            productGroups = detail.productGroups
                        ).also {
                            // Imagens sempre do OFF se disponíveis
                            it.images = offProduct?.images ?: emptyList()
                        }
                    }
                    offProduct != null -> {
                        val units: List<UnitTypeInfo> = ListsStore.unitTypes

                        // Produto novo → tudo vem do OFF
                        ProductDetail(
                            barcode = offResponse.code,
                            name = offProduct.productName ?: "",
                            unitSize = offProduct.productQuantity,
                            categoryId = null,
                            unitId = units.find { it.type.equals(offProduct.productQuantityUnit, ignoreCase = true) }?.id,
                            totalQuantity = null,
                            reservedQuantity = null,
                            availableStock = null,
                            expNotif = null,
                            productGroups = emptyList()
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
     * Carrega lista de produtos e aplica filtros (Texto e Categoria) e Paginação Local
     */
    fun loadProducts(search: String = searchQuery) {
        // Se mudarmos a pesquisa de texto, reset à página
        if (search != searchQuery) {
            currentPage = 1
        }
        searchQuery = search
        isLoading = true
        errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = productRepository.getProducts(search).data

                // 1. Aplicar Filtro de Categoria (Localmente)
                val categoryFilteredList = if (selectedCategoryId != null) {
                    response.filter { it.categoryId == selectedCategoryId }
                } else {
                    response
                }

                stockItems = categoryFilteredList

                // 2. Calcular Paginação baseada na lista filtrada
                totalPages = (categoryFilteredList.size + pageSize - 1) / pageSize
                if (totalPages < 1) totalPages = 1 // Evitar 0 páginas

                // Garantir que a página atual é válida após filtragem
                if (currentPage > totalPages) currentPage = 1

                val startIndex = (currentPage - 1) * pageSize

                // 3. Fatiar a lista para a página atual
                filteredItems = categoryFilteredList.drop(startIndex).take(pageSize)

            } catch (e: Exception) {
                errorMessage = e.message
                filteredItems = emptyList()
                stockItems = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Valida e submete alteração a cabeçalho do produto.
     */
    fun putProduct(
        barcode: String,
        body: ProductPut
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            // inicia limpeza de estado
            _uiState.value = _uiState.value.copy(isLoading = false, errors = emptyMap(), lastErrorMessage = null, success = false)

            val errors = mutableMapOf<String, String>()

            // barcode (necessário para pesquisa)
            if (barcode.isBlank()) {
                errors["barcode"] = "Código de barras obrigatório"
            }

            // Name (Não pode ir vazio)
            if (body.name == null) {
                errors["name"] = "Nome é obrigatório"
            }

            // unitSize (Não pode ser menor que 1 nem vazio)
            if (body.unitSize == null || body.unitSize <= 1) {
                errors["unitSize"] = "Quantidade por unidade tem de ser no mínimo 1"
            }

            // se existirem erros, atualiza estado e sai
            if (errors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errors = errors, lastErrorMessage = "Existem erros no formulário")
                return@launch
            }

            // construir body
            val body = ProductPut(
                name = body.name,
                unitSize = body.unitSize,
                categoryId = body.categoryId,
                unitId = body.unitId,
                expNotif = body.expNotif
            )

            // enviar
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                productRepository.putProduct(barcode,body)
            }.onSuccess { _ ->
                // considera sucesso — volta na navegação
                _uiState.value = ProductUiState(success = true)

            }.onFailure { t ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastErrorMessage = t.message ?: "Erro ao submeter"
                )
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