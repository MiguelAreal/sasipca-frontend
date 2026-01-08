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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sasipca.network.ApiClient
import sasipca.models.ActiveCampaigns
import sasipca.models.Category
import sasipca.models.GroupToEnter
import sasipca.models.SnackbarType
import sasipca.models.UnitType
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.repositories.ReceiptRepository
import sasipca.storage.ListsStore
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.BarcodeInputField
import sasipca.ui.components.products.GroupsSection
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.ReceiptInfoSection
import sasipca.ui.components.products.GroupCard
import sasipca.ui.theme.CardTitle
import sasipca.utils.SnackbarManager
import sasipca.viewmodels.ProductViewModel
import sasipca.viewmodels.ReceiptsViewModel
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(productRepository: ProductRepository, receiptRepository: ReceiptRepository) {

    val receiptsViewModel = remember { ReceiptsViewModel(receiptRepository) }
    val uiState by receiptsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val navigator = LocalNavigator.currentOrThrow

    // Inicializa o ViewModel com o repositório recebido
    val productViewModel = remember { ProductViewModel(productRepository) }
    val offRepository = remember { OFFRepository(ApiClient.client) }

    // Campos do Formulário
    var barcode by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUnit by remember { mutableStateOf<UnitType?>(null) }
    var selectedCampaign by remember { mutableStateOf<ActiveCampaigns?>(null) }

    var note by remember { mutableStateOf("") }
    var groups by remember { mutableStateOf(listOf(GroupToEnter("", ""))) }
    var groupsExpanded by remember { mutableStateOf(true) }

    var editableName by remember { mutableStateOf("") }
    var editableUnitSize by remember { mutableStateOf("") }

    // Estados de Pesquisa (Autocomplete)
    var productQuery by remember { mutableStateOf("") }
    val productSearchResults = productViewModel.filteredItems // Sugestões do VM

    // Observa os estados do ViewModel
    val productDetail = productViewModel.selectedProductDetail
    val isLoadingProduct = productViewModel.isLoading

    // Inicialização de listas
    val categories: List<Category> = remember { ListsStore.categoriestypes.map { Category(it.id, it.type) } }
    val units: List<UnitType> = remember { ListsStore.unitTypes.map { UnitType(it.id, it.type) } }
    val activeCampaigns: List<ActiveCampaigns> = remember { ListsStore.ActiveCampaigns.map { ActiveCampaigns(it.id, it.name) } }
    val images = productDetail?.images ?: emptyList()

    // 1. Atualiza dados do formulário quando o produto muda
    LaunchedEffect(productDetail) {
        editableName = productDetail?.name ?: ""
        editableUnitSize = productDetail?.unitSize?.toString() ?: ""

        selectedUnit = units.find { it.id == productDetail?.unitId }
        selectedCategory = categories.find{it.id == productDetail?.categoryId}
    }

    // 2. Lógica de Pesquisa / Autocomplete
    LaunchedEffect(productQuery) {
        if (productQuery.isEmpty()) return@LaunchedEffect

        delay(400) // Debounce

        val isPotentialBarcode = productQuery.all { it.isDigit() } && productQuery.length >= 8
        if (isPotentialBarcode) {
            barcode = productQuery
        } else {
            productViewModel.loadProducts(search = productQuery)
        }
    }

    // 3. Busca detalhes do produto ao alterar o barcode
    LaunchedEffect(barcode) {
        if (barcode.isNotEmpty()) {
            productViewModel.loadProductHybrid(barcode, offRepository)
        } else {
            productViewModel.resetProduct()
            if(productQuery.isEmpty()) {
                editableName = ""
                editableUnitSize = ""
                selectedCategory = null
                selectedUnit = null
            }
        }
    }

    // 4. Feedback de Sucesso
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            receiptsViewModel.clearUiState()
            navigator.pop()
        }
        if (uiState.lastErrorMessage != null) {
            SnackbarManager.show(uiState.lastErrorMessage!!, SnackbarType.ERROR)
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
                        // Barcode ‘input’ (Atualizado)
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
                                CardTitle("Produto")
                                Spacer(Modifier.height(8.dp))

                                // COMPONENTE NOVO
                                BarcodeInputField(
                                    value = productQuery,
                                    onValueChange = {
                                        productQuery = it
                                        if (it.isEmpty()) barcode = ""
                                    },
                                    error = uiState.errors["barcode"],
                                    suggestions = productSearchResults,
                                    onSuggestionSelected = { product ->
                                        barcode = product.barcode
                                        productQuery = product.name
                                        focusManager.clearFocus()
                                    }
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

                    // Right side - Groups and Register button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        GroupsSection(
                            groups = groups,
                            onAddGroup = { groups = groups + GroupToEnter("", "") },
                            onGroupChange = { index, updatedGroup ->
                                groups = groups.toMutableList().apply { set(index, updatedGroup) }
                            },
                            onRemoveGroup = { index ->
                                if (groups.size > 1) groups = groups.toMutableList().apply { removeAt(index) }
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
                                        groupsUi = groups,
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
                            Text("Registar Receção", fontSize = 16.sp)
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
                                CardTitle("Produto")
                                Spacer(Modifier.height(8.dp))

                                // COMPONENTE NOVO (Mobile)
                                BarcodeInputField(
                                    value = productQuery,
                                    onValueChange = {
                                        productQuery = it
                                        if (it.isEmpty()) barcode = ""
                                    },
                                    error = uiState.errors["barcode"],
                                    suggestions = productSearchResults,
                                    onSuggestionSelected = { product ->
                                        barcode = product.barcode
                                        productQuery = product.name
                                        focusManager.clearFocus()
                                    }
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
                                .clickable { groupsExpanded = !groupsExpanded },
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
                                    CardTitle("Grupos (${groups.size})")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (groupsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = { groups = groups + GroupToEnter("", "") }) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Adicionar grupo")
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AnimatedVisibility(
                                visible = groupsExpanded,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    groups.forEachIndexed { index, group ->
                                        GroupCard(
                                            group = group,
                                            index = index,
                                            onGroupChange = { updatedGroup ->
                                                groups = groups.toMutableList().apply { set(index, updatedGroup) }
                                            },
                                            onRemove = {
                                                if (groups.size > 1) groups = groups.toMutableList().apply { removeAt(index) }
                                            },
                                            canRemove = groups.size > 1
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
                                        groupsUi = groups,
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