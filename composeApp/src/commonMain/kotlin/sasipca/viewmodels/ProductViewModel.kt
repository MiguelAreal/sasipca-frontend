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
import sasipca.navigation.NavigationService
import sasipca.repositories.OFFRepository
import sasipca.storage.ListsStore

data class ProductUiState(
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(), // chave -> mensagem (ex: "barcode" -> "Obrigatório")
    val lastErrorMessage: String? = null,
    val success: Boolean = false
)

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow(ProductUiState())

    val uiState: StateFlow<ProductUiState> = _uiState

    var stockItems by mutableStateOf<List<Product>>(emptyList())
        private set

    var filteredItems by mutableStateOf<List<Product>>(emptyList())
        private set

    var selectedProductDetail by mutableStateOf<ProductDetail?>(null)
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
                    productLots = detail.productLots
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
                            productLots = detail.productLots
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
                            name = offProduct.product_name ?: "",
                            unitSize = offProduct.product_quantity,
                            categoryId = null,
                            unitId = units.find { it.type.equals(offProduct.product_quantity_unit, ignoreCase = true) }?.id,
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
                val response = productRepository.getProducts(search).data
                stockItems = response

                val startIndex = (currentPage - 1) * pageSize
                filteredItems = response.drop(startIndex).take(pageSize)

                totalPages = (response.size + pageSize - 1) / pageSize
            } catch (e: Exception) {
                errorMessage = e.message
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
            )

            // enviar
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                productRepository.putProduct(barcode,body)
            }.onSuccess { response ->
                // considera sucesso — volta atrás na navegação
                _uiState.value = ProductUiState(success = true)

                // navigation
                NavigationService.goBack()
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
