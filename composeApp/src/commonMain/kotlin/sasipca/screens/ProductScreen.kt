package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sasipca.ApiClient
import sasipca.models.Category
import sasipca.models.ProductDetail
import sasipca.models.UnitType
import sasipca.navigation.NavigationService
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.DeliveryRepository
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.storage.ListsStore
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.ScreenSizeManager.updateSize
import sasipca.ui.components.Header
import sasipca.ui.components.beneficiaries.*
import sasipca.ui.components.products.ProductEditForm
import sasipca.viewmodels.BeneficiaryDetailViewModel
import sasipca.viewmodels.DeliveriesViewModel
import sasipca.viewmodels.ProductViewModel

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    barcode: String,
    productRepository: ProductRepository
) {

    val productViewModel = remember { ProductViewModel(productRepository) }
    val uiState by productViewModel.uiState.collectAsState()

    val offRepository = remember { OFFRepository(ApiClient.client) }
    val scope = rememberCoroutineScope()

    val productDetail: ProductDetail? = productViewModel.selectedProductDetail
    val isLoading by remember { productViewModel::isLoading }
    var selectedTab by remember { mutableStateOf(0) }

    // Carrega dados iniciais
    LaunchedEffect(barcode) {
        productViewModel.getProduct(barcode,offRepository)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            title = "Produto",
            subTitle = productDetail?.name?: ""
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
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
                                        productViewModel.putProduct(
                                            barcode = barcode,
                                            body = body
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Coluna da direita - Histórico
                    Box(modifier = Modifier.weight(1f)) {}
                }
            } else {
                // Layout com separadores para ecrãs pequenos
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
                            label = { Text("Stock") }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTab) {
                            0 -> productDetail?.let {
                                ProductEditForm(
                                    product = it,
                                    isLoading = isLoading,
                                    errors = uiState.errors,
                                    onSave = { body ->
                                        scope.launch {
                                            productViewModel.putProduct(
                                                barcode = barcode,
                                                body = body
                                            )
                                        }
                                    }
                                )
                            }
                            1 -> Box {}
                        }
                    }
                }
            }
        }
    }
}



