package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sasipca.models.MovementHistory
import sasipca.network.ApiClient
import sasipca.models.ProductDetail
import sasipca.repositories.HistoryRepository // Importar repo
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.Header
import sasipca.ui.components.products.ProductEditForm
import sasipca.ui.components.products.ProductHistoryTable // Importar componente
import sasipca.viewmodels.ProductViewModel

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    barcode: String,
    productRepository: ProductRepository,
    historyRepository: HistoryRepository = remember { HistoryRepository(ApiClient.client) } // Injeção default ou passada
) {

    val productViewModel = remember { ProductViewModel(productRepository) }
    val uiState by productViewModel.uiState.collectAsState()

    val offRepository = remember { OFFRepository(ApiClient.client) }
    val scope = rememberCoroutineScope()

    val productDetail: ProductDetail? = productViewModel.selectedProductDetail
    val isLoading by remember { productViewModel::isLoading }

    // --- ESTADOS DO HISTÓRICO ---
    var history by remember { mutableStateOf<List<MovementHistory>>(emptyList()) }
    var isHistoryLoading by remember { mutableStateOf(false) }
    // ----------------------------

    var selectedTab by remember { mutableStateOf(0) }

    // Carrega dados iniciais (Produto + Histórico)
    LaunchedEffect(barcode) {
        // 1. Carregar Produto
        productViewModel.getProduct(barcode, offRepository)

        // 2. Carregar Histórico
        isHistoryLoading = true
        history = historyRepository.getProductHistory(barcode)
        isHistoryLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            title = "Produto",
            subTitle = productDetail?.name ?: ""
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
                // --- LAYOUT DESKTOP (LADO A LADO) ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Coluna da esquerda - Edição
                    Box(modifier = Modifier.weight(1f)) {
                        productDetail?.let {
                            ProductEditForm(
                                product = it,
                                isLoading = isLoading,
                                errors = uiState.errors,
                                onSave = { body ->
                                    scope.launch {
                                        productViewModel.putProduct(barcode = barcode, body = body)
                                    }
                                }
                            )
                        }
                    }

                    // Coluna da direita - Histórico
                    Box(modifier = Modifier.weight(1f)) {
                        ProductHistoryTable(
                            history = history,
                            isLoading = isHistoryLoading
                        )
                    }
                }
            } else {
                // --- LAYOUT MOBILE (SEPARADORES) ---
                Column(modifier = Modifier.fillMaxSize()) {

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Produto") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Histórico") } // Mudado de "Stock" para "Histórico" ou podes ter os 3
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        when (selectedTab) {
                            0 -> productDetail?.let {
                                ProductEditForm(
                                    product = it,
                                    isLoading = isLoading,
                                    errors = uiState.errors,
                                    onSave = { body ->
                                        scope.launch {
                                            productViewModel.putProduct(barcode = barcode, body = body)
                                        }
                                    }
                                )
                            }
                            1 -> {
                                ProductHistoryTable(
                                    history = history,
                                    isLoading = isHistoryLoading
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}