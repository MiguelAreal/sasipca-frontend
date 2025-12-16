package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import sasipca.models.MovementHistory
import sasipca.network.ApiClient
import sasipca.models.ProductDetail
import sasipca.repositories.HistoryRepository
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.SessionManager
import sasipca.storage.SessionManager.isAdmin
import sasipca.ui.components.Header
import sasipca.ui.components.products.ProductEditForm
import sasipca.ui.components.products.ProductGroupsTable
import sasipca.ui.components.products.ProductHistoryTable
import sasipca.ui.components.products.ProductImagesCarousel
import sasipca.viewmodels.ProductViewModel

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    barcode: String,
    productRepository: ProductRepository,
    historyRepository: HistoryRepository = remember { HistoryRepository(ApiClient.client) }
) {
    val isReadOnly = remember { !isAdmin() }
    val navigator = LocalNavigator.currentOrThrow

    val productViewModel = remember { ProductViewModel(productRepository) }
    val uiState by productViewModel.uiState.collectAsState()
    val offRepository = remember { OFFRepository(ApiClient.client) }
    val scope = rememberCoroutineScope()

    val productDetail: ProductDetail? = productViewModel.selectedProductDetail

    val isLoadingProduct by remember { productViewModel::isLoading }
    val isSaving by remember { derivedStateOf { uiState.isLoading } }

    var history by remember { mutableStateOf<List<MovementHistory>>(emptyList()) }
    var isHistoryLoading by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }

    val anyLoading = isLoadingProduct || isHistoryLoading || isSaving

    LaunchedEffect(barcode) {
        productViewModel.getProduct(barcode, offRepository)

        if (!isReadOnly) {
            isHistoryLoading = true
            history = historyRepository.getProductHistory(barcode)
            isHistoryLoading = false
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navigator.pop()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            title = "Produto",
            subTitle = productDetail?.name ?: ""
        )

        if (anyLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
                // --- LAYOUT DESKTOP ---
                Row(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Coluna 1: Edição + Imagem em baixo
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Formulário (Topo)
                        Box(modifier = Modifier.weight(1f)) {
                            productDetail?.let {
                                ProductEditForm(
                                    product = it,
                                    isLoading = isSaving,
                                    errors = uiState.errors,
                                    onSave = { body ->
                                        scope.launch { productViewModel.putProduct(barcode, body) }
                                    },
                                    isReadOnly = isReadOnly
                                )
                            }
                        }

                        // Imagem (Fundo da coluna esquerda)
                        if (!productDetail?.images.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().height(200.dp), // Mais pequeno
                                shape = MaterialTheme.shapes.large,
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                ProductImagesCarousel(
                                    images = productDetail!!.images!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Coluna 2: Histórico ou Stock
                    Box(modifier = Modifier.weight(1f)) {
                        if (isReadOnly) {
                            productDetail?.let {
                                ProductGroupsTable(
                                    groups = it.productGroups,
                                    isBeneficiary = true
                                )
                            }
                        } else {
                            ProductHistoryTable(history = history, isLoading = isHistoryLoading)
                        }
                    }
                }
            } else {
                // --- LAYOUT MOBILE ---
                Column(modifier = Modifier.fillMaxSize()) {

                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Detalhes") },
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        if (!isReadOnly) {
                            FilterChip(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("Histórico") },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        val stockTabIndex = if (isReadOnly) 1 else 2
                        FilterChip(
                            selected = selectedTab == stockTabIndex,
                            onClick = { selectedTab = stockTabIndex },
                            label = { Text("Validades") }
                        )
                    }

                    // Conteúdo
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        when (selectedTab) {
                            0 -> {
                                // DETALHES + IMAGEM EM BAIXO
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    productDetail?.let {
                                        // Formulário ocupa espaço flexível, mas deixa espaço para a imagem
                                        Box(modifier = Modifier.weight(1f)) {
                                            ProductEditForm(
                                                product = it,
                                                isLoading = isSaving,
                                                errors = uiState.errors,
                                                onSave = { body ->
                                                    scope.launch { productViewModel.putProduct(barcode, body) }
                                                },
                                                isReadOnly = isReadOnly
                                            )
                                        }

                                        // Imagem em baixo do form
                                        if (!it.images.isNullOrEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                                shape = MaterialTheme.shapes.large,
                                                elevation = CardDefaults.cardElevation(2.dp)
                                            ) {
                                                ProductImagesCarousel(
                                                    images = it.images!!,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (isReadOnly) {
                                    productDetail?.let {
                                        ProductGroupsTable(groups = it.productGroups, isBeneficiary = true)
                                    }
                                } else {
                                    ProductHistoryTable(history = history, isLoading = isHistoryLoading)
                                }
                            }
                            2 -> {
                                if (!isReadOnly) {
                                    productDetail?.let {
                                        ProductGroupsTable(groups = it.productGroups, isBeneficiary = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}