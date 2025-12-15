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
import sasipca.repositories.HistoryRepository
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.SessionManager
import sasipca.ui.components.Header
import sasipca.ui.components.products.ProductEditForm
import sasipca.ui.components.products.ProductGroupsTable // IMPORTAR NOVO
import sasipca.ui.components.products.ProductHistoryTable
import sasipca.viewmodels.ProductViewModel

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    barcode: String,
    productRepository: ProductRepository,
    historyRepository: HistoryRepository = remember { HistoryRepository(ApiClient.client) }
) {
    // 1. Determinar Perfil (ReadOnly = Beneficiário)
    val userRole = remember { SessionManager.getUserRole() }
    val isReadOnly = userRole == "Beneficiary"

    val productViewModel = remember { ProductViewModel(productRepository) }
    val uiState by productViewModel.uiState.collectAsState()
    val offRepository = remember { OFFRepository(ApiClient.client) }
    val scope = rememberCoroutineScope()

    val productDetail: ProductDetail? = productViewModel.selectedProductDetail
    val isLoading by remember { productViewModel::isLoading }

    // --- ESTADOS DO HISTÓRICO ---
    var history by remember { mutableStateOf<List<MovementHistory>>(emptyList()) }
    var isHistoryLoading by remember { mutableStateOf(false) }

    // --- TABS DINÂMICAS ---
    var selectedTab by remember { mutableStateOf(0) }

    // Se for Beneficiário: 0=Produto, 1=Stock(Lotes)
    // Se for Admin: 0=Produto, 1=Histórico, 2=Stock(Lotes)

    // Carrega dados iniciais
    LaunchedEffect(barcode) {
        // 1. Carregar Produto (Detalhes + Grupos)
        productViewModel.getProduct(barcode, offRepository)

        // 2. Carregar Histórico APENAS SE FOR ADMIN
        if (!isReadOnly) {
            isHistoryLoading = true
            history = historyRepository.getProductHistory(barcode)
            isHistoryLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            title = "Produto",
            subTitle = productDetail?.name ?: ""
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
                // --- LAYOUT DESKTOP ---
                Row(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Esquerda: Edição
                    Box(modifier = Modifier.weight(1f)) {
                        productDetail?.let {
                            ProductEditForm(
                                product = it,
                                isLoading = isLoading,
                                errors = uiState.errors,
                                onSave = { body ->
                                    scope.launch { productViewModel.putProduct(barcode, body) }
                                },
                                isReadOnly = isReadOnly // <--- Passar flag
                            )
                        }
                    }

                    // Direita: Histórico ou Stock (depende do perfil)
                    Box(modifier = Modifier.weight(1f)) {
                        if (isReadOnly) {
                            // Beneficiário vê Grupos de Validade à direita
                            productDetail?.let { ProductGroupsTable(it.productGroups) }
                        } else {
                            // Admin vê Histórico por defeito (pode ter tabs internas se quiseres)
                            ProductHistoryTable(history = history, isLoading = isHistoryLoading)
                        }
                    }
                }
            } else {
                // --- LAYOUT MOBILE (TABS) ---
                Column(modifier = Modifier.fillMaxSize()) {

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        // Tab 0: Produto (Sempre)
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Produto") },
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        if (!isReadOnly) {
                            // Admin: Tab 1 = Histórico
                            FilterChip(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("Histórico") },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        // Tab 2 (Admin) ou Tab 1 (Benef): Stock/Validades
                        val stockTabIndex = if (isReadOnly) 1 else 2
                        FilterChip(
                            selected = selectedTab == stockTabIndex,
                            onClick = { selectedTab = stockTabIndex },
                            label = { Text("Validades") }
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
                                        scope.launch { productViewModel.putProduct(barcode, body) }
                                    },
                                    isReadOnly = isReadOnly // <--- Passar flag
                                )
                            }
                            1 -> {
                                if (isReadOnly) {
                                    // Beneficiário: Tab 1 é Stock
                                    productDetail?.let { ProductGroupsTable(it.productGroups) }
                                } else {
                                    // Admin: Tab 1 é Histórico
                                    ProductHistoryTable(history = history, isLoading = isHistoryLoading)
                                }
                            }
                            2 -> {
                                // Admin: Tab 2 é Stock
                                if (!isReadOnly) {
                                    productDetail?.let { ProductGroupsTable(it.productGroups) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}