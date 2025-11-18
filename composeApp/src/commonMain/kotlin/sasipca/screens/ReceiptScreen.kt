package sasipca.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sasipca.ApiClient
import sasipca.models.ActiveCampaigns
import sasipca.models.LotToEnter
import sasipca.models.Category
import sasipca.models.UnitType
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.repositories.ReceiptRepository
import sasipca.storage.ListsStore
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.BarcodeInputField
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.products.LotCard
import sasipca.ui.components.LotsSection
import sasipca.ui.components.ReceiptInfoSection
import sasipca.ui.theme.CardTitle
import sasipca.viewmodels.ProductViewModel
import sasipca.viewmodels.ReceiptsViewModel
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(productRepository: ProductRepository, receiptRepository: ReceiptRepository) {

    val receiptsViewModel = remember { ReceiptsViewModel(receiptRepository) }
    val uiState by receiptsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var barcode by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUnit by remember { mutableStateOf<UnitType?>(null) }
    var selectedCampaign by remember { mutableStateOf<ActiveCampaigns?>(null) }

    var note by remember { mutableStateOf("") }
    var lots by remember { mutableStateOf(listOf(LotToEnter("", "", ""))) }
    var lotsExpanded by remember { mutableStateOf(true) }

    var editableName by remember { mutableStateOf("") }
    var editableUnitSize by remember { mutableStateOf("") }

    val offRepository = remember { OFFRepository(ApiClient.client) }

    // Inicializa o ViewModel com o repositório recebido
    val productViewModel = remember { ProductViewModel(productRepository) }

    // Observa os estados do ViewModel
    val productDetail = productViewModel.selectedProductDetail
    val isLoadingProduct = productViewModel.isLoading

    // Inicialização de listas
    val categories: List<Category> = remember { ListsStore.categoriestypes.map { Category(it.id, it.type) } }
    val units: List<UnitType> = remember { ListsStore.unitTypes.map { UnitType(it.id, it.type) } }
    val activeCampaigns: List<ActiveCampaigns> = remember { ListsStore.ActiveCampaigns.map { ActiveCampaigns(it.id, it.name) } }
    val images = productDetail?.images ?: emptyList()

    // Atualiza selectedUnit automaticamente quando o produto muda
    LaunchedEffect(productDetail) {
        editableName = productDetail?.name ?: ""
        editableUnitSize = productDetail?.unitSize?.toString() ?: ""

        selectedUnit = units.find { it.id == productDetail?.unitId }
    }

    // Busca produto ao alterar o barcode
    LaunchedEffect(barcode) {
        if (barcode.isNotEmpty()) {
            productViewModel.loadProductHybrid(barcode, offRepository)
        } else {
            productViewModel.resetProduct()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Header(title = "Receção de Stock")

        Box(modifier = Modifier.fillMaxSize()) {
            val anyLoading = isLoadingProduct || uiState.isLoading

            if (isLargeScreen()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left side - Product Info (2/3 width)
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Barcode input
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CardTitle("Código de Barras")
                                Spacer(Modifier.height(8.dp))
                                BarcodeInputField(
                                    barcode = barcode,
                                    onBarcodeScanned = { barcode = it} ,
                                    uiState.errors["barcode"]
                                )
                            }
                        }

                        // Product info + campaign + notes
                        ReceiptInfoSection(
                            productName = editableName,
                            onProductNameChange = { editableName = it },
                            images = images,
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it },
                            categories = categories,
                            selectedUnit = selectedUnit,
                            onUnitSelect = { selectedUnit = it },
                            units = units,
                            unitSize = editableUnitSize,
                            onUnitSizeChange = { editableUnitSize = it },
                            selectedCampaign = selectedCampaign,
                            onCampaignSelect = { selectedCampaign = it },
                            campaigns = activeCampaigns,
                            note = note,
                            onNoteChange = { note = it },
                            errors = uiState.errors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Right side - Lots and Register button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        LotsSection(
                            lots = lots,
                            onAddLot = { lots = lots + LotToEnter("", "", "") },
                            onLotChange = { index, updatedLot ->
                                lots = lots.toMutableList().apply { set(index, updatedLot) }
                            },
                            onRemoveLot = { index ->
                                if (lots.size > 1) lots = lots.toMutableList().apply { removeAt(index) }
                            },
                            isWideScreen = true,
                            errors = uiState.errors,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    receiptsViewModel.submitReceipt(
                                        barcode = barcode,
                                        lotsUi = lots,
                                        name = editableName,
                                        categoryId = selectedCategory?.id,
                                        unitId = selectedUnit?.id,
                                        campaignId = selectedCampaign?.id,
                                        unitSizeStr = editableUnitSize,
                                        note = note
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !(isLoadingProduct || uiState.isLoading)
                        ) {
                            Text(
                                "Registar Receção",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                // Compact/mobile screen
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CardTitle("Código de Barras")
                                Spacer(Modifier.height(8.dp))
                                BarcodeInputField(
                                    barcode = barcode,
                                    onBarcodeScanned = { barcode = it} ,
                                    uiState.errors["barcode"]
                                )
                            }
                        }
                    }

                    item {
                        ReceiptInfoSection(
                            productName = editableName,
                            onProductNameChange = { editableName = it },
                            images = images,
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it },
                            categories = categories,
                            selectedUnit = selectedUnit,
                            onUnitSelect = { selectedUnit = it },
                            units = units,
                            unitSize = editableUnitSize,
                            onUnitSizeChange = { editableUnitSize = it },
                            selectedCampaign = selectedCampaign,
                            onCampaignSelect = { selectedCampaign = it },
                            campaigns = activeCampaigns,
                            note = note,
                            errors = uiState.errors,
                            onNoteChange = { note = it }
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { lotsExpanded = !lotsExpanded },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CardTitle("Lotes (${lots.size})")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (lotsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = { lots = lots + LotToEnter("", "", "") }) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Adicionar lote")
                                }
                            }
                        }
                    }

                    item {
                        Column {
                            AnimatedVisibility(
                                visible = lotsExpanded,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    lots.forEachIndexed { index, lot ->
                                        LotCard(
                                            lot = lot,
                                            index = index,
                                            onLotChange = { updatedLot ->
                                                lots = lots.toMutableList().apply { set(index, updatedLot) }
                                            },
                                            onRemove = {
                                                if (lots.size > 1) lots = lots.toMutableList().apply { removeAt(index) }
                                            },
                                            canRemove = lots.size > 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    receiptsViewModel.submitReceipt(
                                        barcode = barcode,
                                        lotsUi = lots,
                                        name = editableName,
                                        categoryId = selectedCategory?.id,
                                        unitId = selectedUnit?.id,
                                        campaignId = selectedCampaign?.id,
                                        unitSizeStr = editableUnitSize,
                                        note = note
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !(isLoadingProduct || uiState.isLoading)
                        ) {
                            Text(
                                "Registar Receção",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            if (anyLoading) {
                LoadingWidget()
            }
        }
    }
}
