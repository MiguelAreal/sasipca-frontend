package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import sasipca.storage.SessionManager.isAdmin
import sasipca.ui.components.Header
import sasipca.ui.components.products.ProductEditForm
import sasipca.ui.components.products.ProductGroupsTable
import sasipca.ui.components.products.ProductHistoryTable
import sasipca.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    barcode: String,
    productRepository: ProductRepository,
    historyRepository: HistoryRepository = remember { HistoryRepository(ApiClient.client) }
) {
    val isReadOnly = remember { !isAdmin() }
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()

    val productViewModel = remember { ProductViewModel(productRepository) }
    val uiState by productViewModel.uiState.collectAsState()
    val offRepository = remember { OFFRepository(ApiClient.client) }

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
        if (uiState.success) navigator.pop()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(title = "Produto", subTitle = productDetail?.name ?: "")

        if (anyLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
                // --- LAYOUT DESKTOP ---
                Row(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Coluna 1: Edição + Imagem (Scroll interno)
                    Column(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                        productDetail?.let { detail ->
                            ProductEditForm(
                                product = detail,
                                isLoading = isSaving,
                                errors = uiState.errors,
                                onSave = { body -> scope.launch { productViewModel.putProduct(barcode, body) } },
                                isReadOnly = isReadOnly,
                                images = detail.images ?: emptyList()
                            )
                        }
                    }

                    // Coluna 2: Histórico e Validades
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (!isReadOnly) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    label = { Text("Histórico", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    label = { Text("Validades", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)) {
                            if (isReadOnly || selectedTab == 1) {
                                productDetail?.let {
                                    ProductGroupsTable(groups = it.productGroups, isBeneficiary = isReadOnly)
                                }
                            } else {
                                ProductHistoryTable(history = history, isLoading = isHistoryLoading)
                            }
                        }
                    }
                }
            } else {
                // --- LAYOUT MOBILE ---
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Detalhes") },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        if (!isReadOnly) {
                            FilterChip(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("Histórico") },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        val stockIndex = if (isReadOnly) 1 else 2
                        FilterChip(
                            selected = selectedTab == stockIndex,
                            onClick = { selectedTab = stockIndex },
                            label = { Text("Validades") },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                        when {
                            selectedTab == 0 -> {
                                productDetail?.let { detail ->
                                    ProductEditForm(
                                        product = detail,
                                        isLoading = isSaving,
                                        errors = uiState.errors,
                                        onSave = { body -> scope.launch { productViewModel.putProduct(barcode, body) } },
                                        isReadOnly = isReadOnly,
                                        images = detail.images ?: emptyList()
                                    )
                                }
                            }
                            (selectedTab == 1 && !isReadOnly) -> {
                                ProductHistoryTable(history = history, isLoading = isHistoryLoading)
                            }
                            else -> {
                                productDetail?.let {
                                    ProductGroupsTable(groups = it.productGroups, isBeneficiary = isReadOnly)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}