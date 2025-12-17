package sasipca.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import sasipca.network.ApiClient
import sasipca.models.Delivery
import sasipca.models.DeliveryItem
import sasipca.models.BeneficiaryItem
import sasipca.models.ProductGroup
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.repositories.DeliveryRepository
import sasipca.repositories.BeneficiaryRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.BarcodeInputField
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.ValidatedDateField
import sasipca.ui.components.ValidatedTextField
import sasipca.ui.theme.CardTitle
import sasipca.utils.SnackbarManager
import sasipca.models.SnackbarType
import sasipca.viewmodels.ProductViewModel
import sasipca.viewmodels.DeliveriesViewModel
import sasipca.viewmodels.BeneficiariesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- MODELO DE UI LOCAL ---
data class DeliveryProductToSend(
    val barcode: String,
    val productName: String,
    val quantityToDeliver: Int = 0,
    val selectedGroups: List<DeliveryItem> = emptyList(),
    // 'availableGroups' contém o stock ajustado (Real + O que já tínhamos reservado)
    val availableGroups: List<ProductGroup> = emptyList(),
    // ID do grupo que o utilizador selecionou manualmente para priorizar (null = FIFO Automático)
    val priorityGroupId: Int? = null,
    val isExpanded: Boolean = false,
    val hasError: Boolean = false
) {
    val totalStock: Int get() = availableGroups.sumOf { it.availableStock }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// --- LÓGICA DE DISTRIBUIÇÃO (Prioridade Manual + FIFO) ---
fun recalculateStockDistribution(
    quantityToExport: Int,
    availableGroups: List<ProductGroup>,
    barcode: String,
    priorityGroupId: Int?
): List<DeliveryItem> {
    var remaining = quantityToExport
    val result = mutableListOf<DeliveryItem>()
    val today = LocalDate.now()

    // 1. Tentar preencher com o Grupo Prioritário (se existir e tiver stock, MESMO QUE ESTEJA EXPIRADO)
    if (priorityGroupId != null) {
        val priorityGroup = availableGroups.find { it.id == priorityGroupId }
        if (priorityGroup != null && priorityGroup.availableStock > 0) {
            val quantityToTake = minOf(remaining, priorityGroup.availableStock)
            if (quantityToTake > 0) {
                result.add(DeliveryItem(barcode = barcode, groupId = priorityGroup.id, quantity = quantityToTake))
                remaining -= quantityToTake
            }
        }
    }

    // 2. Se ainda sobrar quantidade, usar FIFO (excluindo o grupo já usado e GRUPOS EXPIRADOS)
    val sortedGroups = availableGroups
        .filter { it.id != priorityGroupId }
        // Filtra grupos cuja validade já passou
        .filter {
            val exp = it.expiryDate
            if (exp == null) true
            else {
                // Converte data do Kotlinx para Java para comparar
                val expDateJava = LocalDate.of(exp.year, exp.monthNumber, exp.dayOfMonth)
                !expDateJava.isBefore(today)
            }
        }
        .sortedBy { it.expiryDate }

    for (group in sortedGroups) {
        if (remaining <= 0) break
        val quantityToTake = minOf(remaining, group.availableStock)
        if (quantityToTake > 0) {
            result.add(DeliveryItem(barcode = barcode, groupId = group.id, quantity = quantityToTake))
            remaining -= quantityToTake
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    productRepository: ProductRepository,
    deliveryRepository: DeliveryRepository,
    beneficiaryRepository: BeneficiaryRepository,
    initialScheduledDate: LocalDate? = null,
    initialIsScheduled: Boolean = false,
    existingDelivery: Delivery? = null
) {
    val navigator = LocalNavigator.currentOrThrow
    val focusManager = LocalFocusManager.current

    // ViewModels
    val deliveriesViewModel = remember { DeliveriesViewModel(deliveryRepository) }
    val beneficiariesViewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }
    val productViewModel = remember { ProductViewModel(productRepository) }
    val offRepository = remember { OFFRepository(ApiClient.client) }

    // Estados UI
    val deliveryUiState by deliveriesViewModel.uiState.collectAsState()

    // Estados Locais
    var barcode by remember { mutableStateOf("") }
    var productsToDeliver by remember { mutableStateOf(listOf<DeliveryProductToSend>()) }
    var deliveryNote by remember { mutableStateOf(existingDelivery?.note ?: "") }

    // Configuração de Data e Tipo
    var isScheduled by remember {
        mutableStateOf(if (existingDelivery != null) true else initialIsScheduled)
    }

    var scheduledDate by remember {
        mutableStateOf(
            if (existingDelivery != null) {
                try { LocalDate.parse(existingDelivery.scheduledDate) } catch (e: Exception) { LocalDate.now() }
            } else {
                initialScheduledDate
            }
        )
    }

    var isLoadingProduct by remember { mutableStateOf(false) }

    // Beneficiário
    var beneficiaryQuery by remember { mutableStateOf("") }
    var selectedBeneficiary by remember { mutableStateOf<BeneficiaryItem?>(null) }
    var isBeneficiaryDropdownExpanded by remember { mutableStateOf(false) }
    val beneficiarySearchResults by remember { beneficiariesViewModel::beneficiaries }
    val isBeneficiaryLoading by remember { beneficiariesViewModel::isLoading }
    val beneficiaryFocusRequester = remember { FocusRequester() }

    // Produto
    var productQuery by remember { mutableStateOf("") }
    val productSearchResults = productViewModel.filteredItems
    val productDetail = productViewModel.selectedProductDetail

    // --- LÓGICA DE PRÉ-PREENCHIMENTO (EDIÇÃO) ---
    LaunchedEffect(existingDelivery) {
        if (existingDelivery != null) {
            isLoadingProduct = true

            val fullDetails = deliveriesViewModel.getDeliveryDetails(existingDelivery.deliveryId)

            if (fullDetails != null) {
                val ownerName = fullDetails.beneficiaryName ?: existingDelivery.beneficiaryName ?: ""
                try {
                    beneficiariesViewModel.loadBeneficiaries(search = ownerName)
                    delay(300)
                    val beneficiaryMatch = beneficiariesViewModel.beneficiaries?.data?.find {
                        it.beneficiaryId == (fullDetails.beneficiaryId ?: existingDelivery.beneficiaryId)
                    }
                    if (beneficiaryMatch != null) {
                        selectedBeneficiary = beneficiaryMatch
                        beneficiaryQuery = beneficiaryMatch.name
                    } else {
                        beneficiaryQuery = ownerName
                        selectedBeneficiary = BeneficiaryItem(
                            beneficiaryId = fullDetails.beneficiaryId ?: existingDelivery.beneficiaryId ?: 0,
                            name = ownerName,
                            email = ""
                        )
                    }
                } catch (e: Exception) {
                    beneficiaryQuery = ownerName
                }

                val items = fullDetails.items
                if (items.isNotEmpty()) {
                    val itemsGrouped = items.groupBy { it.barcode }

                    itemsGrouped.forEach { (barcode, groupItems) ->
                        if (!barcode.isNullOrEmpty()) {
                            try {
                                val productDetails = productRepository.getProduct(barcode)
                                val totalQtyInThisDelivery = groupItems.sumOf { it.quantity }
                                val apiGroups = productDetails.productGroups ?: emptyList()

                                val adjustedGroups = apiGroups.map { apiGroup ->
                                    val qtyUsedByUs = groupItems
                                        .filter { it.groupId == apiGroup.id }
                                        .sumOf { it.quantity }

                                    apiGroup.copy(availableStock = apiGroup.availableStock + qtyUsedByUs)
                                }

                                val calculatedGroups = recalculateStockDistribution(totalQtyInThisDelivery, adjustedGroups, barcode, null)

                                val totalAvailableAdjusted = adjustedGroups.sumOf { it.availableStock }
                                val hasStockError = totalQtyInThisDelivery > totalAvailableAdjusted && existingDelivery.statusId != 1

                                val productToSend = DeliveryProductToSend(
                                    barcode = barcode,
                                    productName = productDetails.name ?: "Produto",
                                    quantityToDeliver = totalQtyInThisDelivery,
                                    availableGroups = adjustedGroups,
                                    selectedGroups = calculatedGroups,
                                    priorityGroupId = null,
                                    hasError = hasStockError
                                )

                                if (productsToDeliver.none { it.barcode == barcode }) {
                                    productsToDeliver = productsToDeliver + productToSend
                                }

                            } catch (e: Exception) {
                                println("Erro ao carregar produto $barcode: ${e.message}")
                            }
                        }
                    }
                }
            } else {
                SnackbarManager.show("Erro ao carregar detalhes da entrega.", SnackbarType.ERROR)
            }
            isLoadingProduct = false
        }
    }

    // --- FEEDBACK ---
    LaunchedEffect(deliveryUiState.success) {
        if (deliveryUiState.success) {
            deliveriesViewModel.clearUiState()
            navigator.pop()
        }
    }

    LaunchedEffect(deliveryUiState.lastErrorMessage) {
        deliveryUiState.lastErrorMessage?.let { SnackbarManager.show(it, SnackbarType.ERROR) }
    }

    // --- LOGICA DE INPUTS ---
    LaunchedEffect(beneficiaryQuery) {
        if (selectedBeneficiary != null && beneficiaryQuery != selectedBeneficiary?.name) {
            selectedBeneficiary = null
        }
        if (beneficiaryQuery.length >= 2 && selectedBeneficiary == null) {
            delay(300)
            beneficiariesViewModel.loadBeneficiaries(search = beneficiaryQuery)
            isBeneficiaryDropdownExpanded = true
        } else {
            isBeneficiaryDropdownExpanded = false
        }
    }

    LaunchedEffect(productQuery) {
        if (productQuery.isEmpty()) return@LaunchedEffect
        delay(400)
        val isPotentialBarcode = productQuery.all { it.isDigit() } && productQuery.length >= 8
        if (isPotentialBarcode) barcode = productQuery
        else productViewModel.loadProducts(search = productQuery)
    }

    // --- ADICIONAR NOVO PRODUTO ---
    LaunchedEffect(barcode) {
        if (barcode.isNotEmpty()) {
            val existingIndex = productsToDeliver.indexOfFirst { it.barcode == barcode }
            if (existingIndex >= 0) {
                val currentProduct = productsToDeliver[existingIndex]
                val newTotal = currentProduct.quantityToDeliver + 1
                if (newTotal <= currentProduct.totalStock) {
                    val newGroups = recalculateStockDistribution(newTotal, currentProduct.availableGroups, barcode, currentProduct.priorityGroupId)
                    val updatedList = productsToDeliver.toMutableList()
                    updatedList[existingIndex] = currentProduct.copy(quantityToDeliver = newTotal, selectedGroups = newGroups, hasError = false)
                    productsToDeliver = updatedList
                    barcode = ""
                    productQuery = ""
                } else {
                    SnackbarManager.show("Stock insuficiente.", SnackbarType.WARNING)
                    barcode = ""
                }
            } else {
                isLoadingProduct = true
                productViewModel.getProduct(barcode, offRepository)
            }
        } else {
            productViewModel.resetProduct()
        }
    }

    // --- PROCESSAR DETALHE PRODUTO (API) ---
    LaunchedEffect(productDetail) {
        if (productDetail != null && barcode.isNotEmpty()) {
            val groups = productDetail.productGroups ?: emptyList()
            if (groups.isEmpty()) {
                SnackbarManager.show("Produto sem stock.", SnackbarType.ERROR)
                barcode = ""
            } else {
                val initialQty = 1
                val calculatedGroups = recalculateStockDistribution(initialQty, groups, barcode, null)
                val newProduct = DeliveryProductToSend(
                    barcode = barcode,
                    productName = productDetail.name ?: "Produto",
                    quantityToDeliver = initialQty,
                    availableGroups = groups,
                    selectedGroups = calculatedGroups,
                    priorityGroupId = null,
                    hasError = initialQty > groups.sumOf { it.availableStock }
                )
                if (!productsToDeliver.any { it.barcode == barcode }) {
                    productsToDeliver = productsToDeliver + newProduct
                    barcode = ""
                    productQuery = ""
                }
            }
            isLoadingProduct = false
        } else if (isLoadingProduct && barcode.isNotEmpty() && productViewModel.errorMessage != null) {
            isLoadingProduct = false
            SnackbarManager.show("Produto não encontrado.", SnackbarType.ERROR)
            barcode = ""
        }
    }

    // --- UI LAYOUT ---
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(title = if (existingDelivery != null) "Editar Entrega" else "Agendamento de Entrega")

        Box(modifier = Modifier.fillMaxSize()) {
            val anyLoading = isLoadingProduct || deliveryUiState.isLoading

            if (isLargeScreen()) {
                // DESKTOP LAYOUT
                Row(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BeneficiarySelectorCard(
                            query = beneficiaryQuery,
                            onQueryChange = { beneficiaryQuery = it },
                            selectedBeneficiary = selectedBeneficiary,
                            onSelect = { selectedBeneficiary = it; beneficiaryQuery = it.name; isBeneficiaryDropdownExpanded = false },
                            expanded = isBeneficiaryDropdownExpanded,
                            onExpandedChange = { isBeneficiaryDropdownExpanded = it },
                            results = beneficiarySearchResults?.data ?: emptyList(),
                            isLoading = isBeneficiaryLoading,
                            focusRequester = beneficiaryFocusRequester,
                            hasError = deliveryUiState.errors.containsKey("beneficiary")
                        )
                        ProductAddCard(
                            query = productQuery,
                            onQueryChange = { productQuery = it; if (it.isEmpty()) barcode = "" },
                            suggestions = productSearchResults,
                            onSuggestionSelected = { barcode = it.barcode; productQuery = ""; focusManager.clearFocus() }
                        )
                        DeliveryOptionsCard(
                            isScheduled = isScheduled,
                            scheduledDate = scheduledDate,
                            onTypeChange = { isScheduled = it },
                            onDateChange = { scheduledDate = it },
                            deliveryNote = deliveryNote,
                            onNoteChange = { deliveryNote = it },
                            dateError = deliveryUiState.errors["date"]
                        )
                    }
                    Column(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                        DeliveryProductsListSection(
                            products = productsToDeliver,
                            onProductRemove = { index -> productsToDeliver = productsToDeliver.toMutableList().apply { removeAt(index) } },
                            onUpdateQuantity = { index, newTotal ->
                                val p = productsToDeliver[index]
                                val isValid = newTotal <= p.totalStock
                                val newGroups = recalculateStockDistribution(newTotal, p.availableGroups, p.barcode, p.priorityGroupId)
                                productsToDeliver = productsToDeliver.toMutableList().apply {
                                    set(index, p.copy(quantityToDeliver = newTotal, selectedGroups = newGroups, hasError = !isValid))
                                }
                            },
                            onPriorityGroupChange = { index, groupId ->
                                val p = productsToDeliver[index]
                                val newGroups = recalculateStockDistribution(p.quantityToDeliver, p.availableGroups, p.barcode, groupId)
                                productsToDeliver = productsToDeliver.toMutableList().apply {
                                    set(index, p.copy(priorityGroupId = groupId, selectedGroups = newGroups))
                                }
                            },
                            onProductExpanded = { index ->
                                val p = productsToDeliver[index]
                                productsToDeliver = productsToDeliver.toMutableList().apply { set(index, p.copy(isExpanded = !p.isExpanded)) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.height(12.dp))
                        SubmitButton(
                            enabled = !anyLoading,
                            text = if (existingDelivery != null) "Guardar Alterações" else "Agendar Entrega",
                            onClick = {
                                if (existingDelivery != null) {
                                    deliveriesViewModel.updateDelivery(
                                        deliveryId = existingDelivery.deliveryId,
                                        beneficiaryId = selectedBeneficiary?.beneficiaryId,
                                        scheduledDate = scheduledDate,
                                        isScheduled = isScheduled,
                                        products = productsToDeliver,
                                        note = deliveryNote,
                                        currentStatusId = existingDelivery.statusId
                                    )
                                } else {
                                    deliveriesViewModel.scheduleDelivery(
                                        beneficiaryId = selectedBeneficiary?.beneficiaryId,
                                        scheduledDate = scheduledDate,
                                        isScheduled = isScheduled,
                                        products = productsToDeliver,
                                        note = deliveryNote
                                    )
                                }
                            },
                            isLoading = anyLoading
                        )
                        if (existingDelivery != null) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { deliveriesViewModel.deleteDelivery(existingDelivery.deliveryId) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar Entrega")
                            }
                        }
                    }
                }
            } else {
                // MOBILE LAYOUT
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    item {
                        BeneficiarySelectorCard(
                            query = beneficiaryQuery,
                            onQueryChange = { beneficiaryQuery = it },
                            selectedBeneficiary = selectedBeneficiary,
                            onSelect = { selectedBeneficiary = it; beneficiaryQuery = it.name; isBeneficiaryDropdownExpanded = false },
                            expanded = isBeneficiaryDropdownExpanded,
                            onExpandedChange = { isBeneficiaryDropdownExpanded = it },
                            results = beneficiarySearchResults?.data ?: emptyList(),
                            isLoading = isBeneficiaryLoading,
                            focusRequester = beneficiaryFocusRequester,
                            hasError = deliveryUiState.errors.containsKey("beneficiary")
                        )
                    }
                    item {
                        ProductAddCard(
                            query = productQuery,
                            onQueryChange = { productQuery = it; if (it.isEmpty()) barcode = "" },
                            suggestions = productSearchResults,
                            onSuggestionSelected = { barcode = it.barcode; productQuery = ""; focusManager.clearFocus() }
                        )
                    }
                    itemsIndexed(productsToDeliver) { index, product ->
                        DeliveryProductCard(
                            product = product,
                            index = index,
                            onRemove = { productsToDeliver = productsToDeliver.toMutableList().apply { removeAt(index) } },
                            onExpand = {
                                val p = productsToDeliver[index]
                                productsToDeliver = productsToDeliver.toMutableList().apply { set(index, p.copy(isExpanded = !p.isExpanded)) }
                            },
                            onUpdateQuantity = { newTotal ->
                                val p = productsToDeliver[index]
                                val isValid = newTotal <= p.totalStock
                                val newGroups = recalculateStockDistribution(newTotal, p.availableGroups, p.barcode, p.priorityGroupId)
                                productsToDeliver = productsToDeliver.toMutableList().apply {
                                    set(index, p.copy(quantityToDeliver = newTotal, selectedGroups = newGroups, hasError = !isValid))
                                }
                            },
                            onPriorityGroupChange = { groupId ->
                                val p = productsToDeliver[index]
                                val newGroups = recalculateStockDistribution(p.quantityToDeliver, p.availableGroups, p.barcode, groupId)
                                productsToDeliver = productsToDeliver.toMutableList().apply {
                                    set(index, p.copy(priorityGroupId = groupId, selectedGroups = newGroups))
                                }
                            }
                        )
                    }
                    item {
                        DeliveryOptionsCard(
                            isScheduled = isScheduled,
                            scheduledDate = scheduledDate,
                            onTypeChange = { isScheduled = it },
                            onDateChange = { scheduledDate = it },
                            deliveryNote = deliveryNote,
                            onNoteChange = { deliveryNote = it },
                            dateError = deliveryUiState.errors["date"]
                        )
                    }
                    item {
                        SubmitButton(
                            enabled = !anyLoading,
                            text = if (existingDelivery != null) "Guardar Alterações" else "Agendar Entrega",
                            onClick = {
                                if (existingDelivery != null) {
                                    deliveriesViewModel.updateDelivery(
                                        deliveryId = existingDelivery.deliveryId,
                                        beneficiaryId = selectedBeneficiary?.beneficiaryId,
                                        scheduledDate = scheduledDate,
                                        isScheduled = isScheduled,
                                        products = productsToDeliver,
                                        note = deliveryNote,
                                        currentStatusId = existingDelivery.statusId
                                    )
                                } else {
                                    deliveriesViewModel.scheduleDelivery(
                                        beneficiaryId = selectedBeneficiary?.beneficiaryId,
                                        scheduledDate = scheduledDate,
                                        isScheduled = isScheduled,
                                        products = productsToDeliver,
                                        note = deliveryNote
                                    )
                                }
                            },
                            isLoading = anyLoading
                        )
                    }
                    if (existingDelivery != null) {
                        item {
                            OutlinedButton(
                                onClick = { deliveriesViewModel.deleteDelivery(existingDelivery.deliveryId) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar Entrega")
                            }
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

// ----------------------------------------------------
// COMPONENTES AUXILIARES
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarySelectorCard(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedBeneficiary: BeneficiaryItem?,
    onSelect: (BeneficiaryItem) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    results: List<BeneficiaryItem>,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    hasError: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardTitle("Beneficiário")
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandedChange(it); if (it) focusRequester.requestFocus() }
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Pesquisar") },
                    placeholder = { Text("Nome ou Email...") },
                    modifier = Modifier.fillMaxWidth().menuAnchor().focusRequester(focusRequester),
                    trailingIcon = {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Search, null)
                    },
                    singleLine = true,
                    isError = (query.isNotEmpty() && selectedBeneficiary == null) || hasError
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.exposedDropdownSize(true).background(MaterialTheme.colorScheme.surface),
                    properties = PopupProperties(focusable = false)
                ) {
                    if (results.isEmpty() && !isLoading) {
                        DropdownMenuItem(text = { Text("Sem resultados") }, onClick = {}, enabled = false)
                    } else {
                        results.forEach { beneficiary ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(beneficiary.name, style = MaterialTheme.typography.bodyLarge)
                                        Text(beneficiary.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = { onSelect(beneficiary) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductAddCard(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<sasipca.models.Product>,
    onSuggestionSelected: (sasipca.models.Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardTitle("Adicionar Produto")
            Spacer(Modifier.height(8.dp))

            BarcodeInputField(
                value = query,
                onValueChange = onQueryChange,
                label = "Pesquisar Produto",
                placeholder = "Nome ou Código...",
                error = null,
                suggestions = suggestions,
                onSuggestionSelected = onSuggestionSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DeliveryOptionsCard(
    isScheduled: Boolean,
    scheduledDate: LocalDate?,
    onTypeChange: (Boolean) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    deliveryNote: String,
    onNoteChange: (String) -> Unit,
    dateError: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardTitle("Detalhes")
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onTypeChange(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isScheduled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isScheduled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("Imediata") }
                Button(
                    onClick = { onTypeChange(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScheduled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isScheduled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("Agendar") }
            }

            if (isScheduled) {
                Spacer(Modifier.height(12.dp))
                ValidatedDateField(
                    value = scheduledDate?.format(dateFormatter) ?: "",
                    onValueChange = { str ->
                        if (str.isNotBlank()) {
                            try {
                                onDateChange(LocalDate.parse(str, dateFormatter))
                            } catch (_: Exception) {}
                        }
                    },
                    label = "Data Entrega",
                    error = dateError,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            ValidatedTextField(
                value = deliveryNote,
                onValueChange = onNoteChange,
                label = "Observações",
                singleLine = false,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
        }
    }
}

@Composable
fun SubmitButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
        else Text(text, fontSize = 16.sp)
    }
}

@Composable
fun DeliveryProductsListSection(
    products: List<DeliveryProductToSend>,
    onProductRemove: (Int) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onPriorityGroupChange: (Int, Int?) -> Unit,
    onProductExpanded: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CardTitle(title = "Produtos (${products.size})")
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                itemsIndexed(products) { index, product ->
                    DeliveryProductCard(
                        product = product,
                        index = index,
                        onRemove = { onProductRemove(index) },
                        onExpand = { onProductExpanded(index) },
                        onUpdateQuantity = { qty -> onUpdateQuantity(index, qty) },
                        onPriorityGroupChange = { groupId -> onPriorityGroupChange(index, groupId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryProductCard(
    product: DeliveryProductToSend,
    index: Int,
    onRemove: () -> Unit,
    onExpand: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onPriorityGroupChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onExpand() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.productName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Qtd.: ${product.quantityToDeliver} | Total Disp.: ${product.totalStock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    Icon(imageVector = if (product.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Remover", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            AnimatedVisibility(visible = product.isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val selectedGroup = product.availableGroups.find { it.id == product.priorityGroupId }

                    val selectionText = if (selectedGroup != null) {
                        val date = selectedGroup.expiryDate
                        val dateStr = if(date != null) "${date.dayOfMonth}/${date.monthNumber}/${date.year}" else "S/D"
                        "$dateStr (${selectedGroup.availableStock} un)"
                    } else {
                        "Automático"
                    }

                    Text("Grupo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectionText,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Automático", style = MaterialTheme.typography.bodyMedium)
                                        Text("Prioriza validade mais curta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                onClick = {
                                    onPriorityGroupChange(null)
                                    dropdownExpanded = false
                                }
                            )
                            Divider()
                            product.availableGroups.sortedBy { it.expiryDate }.forEach { group ->
                                val date = group.expiryDate
                                val dateStr = if(date != null) "${date.dayOfMonth}/${date.monthNumber}/${date.year}" else "S/D"

                                DropdownMenuItem(
                                    text = { Text("$dateStr - Stock: ${group.availableStock}") },
                                    onClick = {
                                        onPriorityGroupChange(group.id)
                                        dropdownExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Quantidade a Exportar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))

                    ValidatedTextField(
                        value = if (product.quantityToDeliver == 0) "" else product.quantityToDeliver.toString(),
                        onValueChange = { onUpdateQuantity(it.toIntOrNull() ?: 0) },
                        label = "Total",
                        error = if (product.hasError) "Quantidade excede o stock disponível" else null,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    if (product.selectedGroups.isNotEmpty()) {
                        Text("Distribuição:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        product.selectedGroups.forEach { item ->
                            val groupInfo = product.availableGroups.find { it.id == item.groupId }
                            val date = groupInfo?.expiryDate
                            val formattedDate = if(date != null) "${date.dayOfMonth}/${date.monthNumber}/${date.year}" else "N/A"

                            // Verifica se o lote está expirado
                            val isExpired = if (date != null) {
                                // Converte para Java LocalDate para usar isBefore com a data atual (Java)
                                val dateJava = LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
                                dateJava.isBefore(LocalDate.now())
                            } else false

                            val isPriority = item.groupId == product.priorityGroupId

                            // Lógica de cores
                            val textColor = if (isExpired) {
                                MaterialTheme.colorScheme.error
                            } else if (isPriority) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }

                            val fontWeight = if (isPriority || isExpired) FontWeight.Bold else FontWeight.Normal

                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Validade: $formattedDate", style = MaterialTheme.typography.bodySmall, color = textColor, fontWeight = fontWeight)
                                Text("${item.quantity} uni.", style = MaterialTheme.typography.bodySmall, color = textColor, fontWeight = fontWeight)
                            }
                        }
                    }
                }
            }
        }
    }
}