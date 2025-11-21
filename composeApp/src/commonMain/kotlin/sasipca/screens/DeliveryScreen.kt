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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import sasipca.ApiClient
import sasipca.models.DeliveryItem
import sasipca.models.BeneficiaryItem
import sasipca.models.ProductLot // IMPORTANTE: Usar o modelo existente
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.repositories.DeliveryRepository
import sasipca.repositories.BeneficiaryRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.ValidatedDateField
import sasipca.ui.theme.CardTitle
import sasipca.utils.SnackbarManager
import sasipca.utils.SnackbarType
import sasipca.viewmodels.ProductViewModel
import sasipca.viewmodels.DeliveriesViewModel
import sasipca.viewmodels.BeneficiariesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- MODELOS DE UI (Wrappers necessários) ---

// Mantemos este wrapper pois ele guarda estado de UI (expandido, erro, quantidade total a enviar)
// mas agora usa os modelos oficiais da API internamente.
data class DeliveryProductToSend(
    val barcode: String,
    val productName: String,
    val quantityToDeliver: Int = 0,
    val selectedLots: List<DeliveryItem> = emptyList(), // Modelo existente
    val availableLots: List<ProductLot> = emptyList(),  // Modelo existente (substitui LotInfo)
    val isExpanded: Boolean = false,
    val hasError: Boolean = false
) {
    val totalStock: Int
        get() = availableLots.sumOf { it.availableStock } // Propriedade direta de ProductLot
}

// --- LÓGICA FIFO ---
fun recalculateLotsFIFO(
    quantityToExport: Int,
    availableLots: List<ProductLot>, // Agora recebe ProductLot
    barcode: String
): List<DeliveryItem> {
    var remaining = quantityToExport
    val result = mutableListOf<DeliveryItem>()
    // Ordena por data de validade
    val sortedLots = availableLots.sortedBy { it.expiryDate }

    for (lot in sortedLots) {
        if (remaining <= 0) break
        val quantityToTake = minOf(remaining, lot.availableStock) // Propriedade direta de ProductLot
        if (quantityToTake > 0) {
            result.add(DeliveryItem(barcode = barcode, lot = lot.lot, quantity = quantityToTake))
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
    beneficiaryRepository: BeneficiaryRepository
) {
    val scope = rememberCoroutineScope()

    // ViewModels
    val deliveriesViewModel = remember { DeliveriesViewModel(deliveryRepository) }
    val beneficiariesViewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }
    val productViewModel = remember { ProductViewModel(productRepository) }
    val offRepository = remember { OFFRepository(ApiClient.client) }

    // Observar UI State do DeliveriesViewModel
    val deliveryUiState by deliveriesViewModel.uiState.collectAsState()

    // Estados Produto e Entrega
    var barcode by remember { mutableStateOf("") }
    var productsToDeliver by remember { mutableStateOf(listOf<DeliveryProductToSend>()) }
    var deliveryNote by remember { mutableStateOf("") }

    var isScheduled by remember { mutableStateOf(false) }
    var scheduledDate by remember { mutableStateOf<LocalDate?>(null) }

    var isLoadingProduct by remember { mutableStateOf(false) }

    // --- ESTADOS AUTOCOMPLETE BENEFICIÁRIO ---
    var beneficiaryQuery by remember { mutableStateOf("") }
    var selectedBeneficiary by remember { mutableStateOf<BeneficiaryItem?>(null) }
    var isBeneficiaryDropdownExpanded by remember { mutableStateOf(false) }
    val beneficiarySearchResults by remember { beneficiariesViewModel::beneficiaries }
    val isBeneficiaryLoading by remember { beneficiariesViewModel::isLoading }
    val beneficiaryFocusRequester = remember { FocusRequester() }

    // --- ESTADOS AUTOCOMPLETE PRODUTO ---
    var productQuery by remember { mutableStateOf("") }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }
    val productSearchResults by remember { productViewModel::filteredItems }
    val isProductSearchLoading by remember { productViewModel::isLoading }
    val productFocusRequester = remember { FocusRequester() }

    val productDetail = productViewModel.selectedProductDetail

    // --- EFEITOS COLATERAIS DE SUBMISSÃO ---
    LaunchedEffect(deliveryUiState.success) {
        if (deliveryUiState.success) {
            // USA A MENSAGEM VINDA DA API, OU UMA DEFAULT
            val message = deliveryUiState.successMessage ?: "Entrega registada com sucesso!"
            SnackbarManager.show(message, SnackbarType.SUCCESS)

            // Limpar
            productsToDeliver = emptyList()
            selectedBeneficiary = null
            beneficiaryQuery = ""
            deliveryNote = ""
            isScheduled = false
            scheduledDate = null

            deliveriesViewModel.clearUiState()
        }
    }

    LaunchedEffect(deliveryUiState.lastErrorMessage) {
        deliveryUiState.lastErrorMessage?.let { msg ->
            SnackbarManager.show(msg, SnackbarType.ERROR)
        }
    }

    // --- LÓGICA DE PESQUISA BENEFICIÁRIO ---
    LaunchedEffect(beneficiaryQuery) {
        if (selectedBeneficiary == null || beneficiaryQuery != selectedBeneficiary?.name) {
            if (selectedBeneficiary != null && beneficiaryQuery != selectedBeneficiary?.name) {
                selectedBeneficiary = null
            }
            if (beneficiaryQuery.length >= 2) {
                delay(250)
                beneficiariesViewModel.loadBeneficiaries(search = beneficiaryQuery)
                isBeneficiaryDropdownExpanded = true
            } else {
                isBeneficiaryDropdownExpanded = false
            }
        }
    }

    // --- LÓGICA DE PESQUISA PRODUTO E AUTO-SCAN ---
    LaunchedEffect(productQuery) {
        if (productQuery.isEmpty()) {
            isProductDropdownExpanded = false
            return@LaunchedEffect
        }
        delay(400) // Delay para scan

        val isPotentialBarcode = productQuery.all { it.isDigit() } && productQuery.length >= 8

        if (isPotentialBarcode) {
            barcode = productQuery
            isProductDropdownExpanded = false
        } else {
            productViewModel.loadProducts(search = productQuery)
            isProductDropdownExpanded = true
        }
    }

    // --- LÓGICA PRINCIPAL DE ADIÇÃO ---
    LaunchedEffect(barcode) {
        if (barcode.isNotEmpty()) {
            val existingIndex = productsToDeliver.indexOfFirst { it.barcode == barcode }
            if (existingIndex >= 0) {
                val currentProduct = productsToDeliver[existingIndex]
                val newTotal = currentProduct.quantityToDeliver + 1
                if (newTotal <= currentProduct.totalStock) {
                    val newLots = recalculateLotsFIFO(newTotal, currentProduct.availableLots, barcode)
                    val updatedList = productsToDeliver.toMutableList()
                    updatedList[existingIndex] = currentProduct.copy(
                        quantityToDeliver = newTotal,
                        selectedLots = newLots,
                        hasError = false
                    )
                    productsToDeliver = updatedList
                    barcode = ""
                    productQuery = ""
                } else {
                    SnackbarManager.show("Stock insuficiente para adicionar mais um item.", SnackbarType.WARNING)
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

    LaunchedEffect(productDetail) {
        if (productDetail != null && barcode.isNotEmpty()) {
            // Agora usamos diretamente a lista de ProductLot que vem do productDetail
            val lots = productDetail.productLots ?: emptyList()

            if (lots.isEmpty()) {
                SnackbarManager.show("Produto sem stock disponível.", SnackbarType.ERROR)
                barcode = ""
                isLoadingProduct = false
                return@LaunchedEffect
            }

            val initialQty = 1
            val calculatedLots = recalculateLotsFIFO(initialQty, lots, barcode)

            val newProduct = DeliveryProductToSend(
                barcode = barcode,
                productName = productDetail.name ?: "Produto sem nome",
                quantityToDeliver = initialQty,
                availableLots = lots, // Passamos a lista de ProductLot diretamente
                selectedLots = calculatedLots,
                hasError = initialQty > lots.sumOf { it.availableStock }
            )

            if (!productsToDeliver.any { it.barcode == barcode }) {
                productsToDeliver = productsToDeliver + newProduct
                barcode = ""
                productQuery = ""
                isLoadingProduct = false
            }
        } else if (isLoadingProduct && barcode.isNotEmpty()) {
            isLoadingProduct = false
            if (productDetail == null) {
                SnackbarManager.show("Produto não encontrado", SnackbarType.ERROR)
                barcode = ""
            }
        }
    }

    // --- UI ---
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(title = "Agendamento de Entrega")

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
                Row(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                    val leftColumnScrollState = rememberScrollState()

                    // Coluna Esquerda
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(leftColumnScrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // CARD BENEFICIÁRIO
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                CardTitle("Beneficiário")
                                Spacer(Modifier.height(8.dp))

                                ExposedDropdownMenuBox(
                                    expanded = isBeneficiaryDropdownExpanded,
                                    onExpandedChange = {
                                        isBeneficiaryDropdownExpanded = it
                                        if(it) beneficiaryFocusRequester.requestFocus()
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = beneficiaryQuery,
                                        onValueChange = {
                                            beneficiaryQuery = it
                                            if (selectedBeneficiary != null && it != selectedBeneficiary?.name) {
                                                selectedBeneficiary = null
                                            }
                                            beneficiaryFocusRequester.requestFocus()
                                        },
                                        label = { Text("Pesquisar Beneficiário") },
                                        placeholder = { Text("Digite o nome...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .focusRequester(beneficiaryFocusRequester),
                                        trailingIcon = {
                                            if (isBeneficiaryLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                                            }
                                        },
                                        singleLine = true,
                                        isError = (beneficiaryQuery.isNotEmpty() && selectedBeneficiary == null) ||
                                                deliveryUiState.errors.containsKey("beneficiary")
                                    )

                                    DropdownMenu(
                                        expanded = isBeneficiaryDropdownExpanded,
                                        onDismissRequest = { isBeneficiaryDropdownExpanded = false },
                                        modifier = Modifier
                                            .exposedDropdownSize(true)
                                            .background(MaterialTheme.colorScheme.surface),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        val list = beneficiarySearchResults?.data ?: emptyList()
                                        if (list.isEmpty() && !isBeneficiaryLoading) {
                                            DropdownMenuItem(text = { Text("Sem resultados") }, onClick = {}, enabled = false)
                                        } else {
                                            list.forEach { beneficiary ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(text = beneficiary.name, style = MaterialTheme.typography.bodyLarge)
                                                            Text(text = beneficiary.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedBeneficiary = beneficiary
                                                        beneficiaryQuery = beneficiary.name
                                                        isBeneficiaryDropdownExpanded = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CARD PRODUTO
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                CardTitle("Adicionar Produto")
                                Spacer(Modifier.height(8.dp))

                                ExposedDropdownMenuBox(
                                    expanded = isProductDropdownExpanded,
                                    onExpandedChange = {
                                        isProductDropdownExpanded = it
                                        if(it) productFocusRequester.requestFocus()
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = productQuery,
                                        onValueChange = {
                                            productQuery = it
                                            productFocusRequester.requestFocus()
                                        },
                                        label = { Text("Produto") },
                                        placeholder = { Text("Nome ou Código de Barras") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .focusRequester(productFocusRequester),
                                        trailingIcon = {
                                            if (isProductSearchLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (productQuery.isNotEmpty()) {
                                                    barcode = productQuery
                                                    isProductDropdownExpanded = false
                                                }
                                            }
                                        )
                                    )

                                    DropdownMenu(
                                        expanded = isProductDropdownExpanded,
                                        onDismissRequest = { isProductDropdownExpanded = false },
                                        modifier = Modifier
                                            .exposedDropdownSize(true)
                                            .background(MaterialTheme.colorScheme.surface),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        val list = productSearchResults
                                        if (list.isEmpty() && !isProductSearchLoading) {
                                            DropdownMenuItem(text = { Text("Não encontrado") }, onClick = {}, enabled = false)
                                        } else {
                                            list.forEach { product ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(text = product.name ?: "Sem Nome", style = MaterialTheme.typography.bodyLarge)
                                                            Text(text = product.barcode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        barcode = product.barcode
                                                        isProductDropdownExpanded = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        DeliveryOptionsCard(
                            isScheduled = isScheduled,
                            scheduledDate = scheduledDate,
                            onTypeChange = { isScheduled = it },
                            onDateChange = { scheduledDate = it },
                            deliveryNote = deliveryNote,
                            onNoteChange = { deliveryNote = it },
                            dateError = deliveryUiState.errors["date"]
                        )

                        Spacer(Modifier.height(16.dp))
                    }

                    // Coluna Direita
                    Column(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                        DeliveryProductsListSection(
                            products = productsToDeliver,
                            onProductRemove = { index -> productsToDeliver = productsToDeliver.toMutableList().apply { removeAt(index) } },
                            onUpdateQuantity = { index, newTotal ->
                                val product = productsToDeliver[index]
                                val isValid = newTotal <= product.totalStock
                                val newLots = recalculateLotsFIFO(newTotal, product.availableLots, product.barcode)
                                val updatedList = productsToDeliver.toMutableList()
                                updatedList[index] = product.copy(quantityToDeliver = newTotal, selectedLots = newLots, hasError = !isValid)
                                productsToDeliver = updatedList
                            },
                            onProductExpanded = { index ->
                                val updated = productsToDeliver.toMutableList()
                                updated[index] = updated[index].copy(isExpanded = !updated[index].isExpanded)
                                productsToDeliver = updated
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.height(12.dp))
                        SubmitButton(
                            enabled = !deliveryUiState.isLoading,
                            onClick = {
                                deliveriesViewModel.scheduleDelivery(
                                    beneficiaryId = selectedBeneficiary?.beneficiaryId,
                                    scheduledDate = scheduledDate,
                                    isScheduled = isScheduled,
                                    products = productsToDeliver,
                                    note = deliveryNote
                                )
                            },
                            isLoading = deliveryUiState.isLoading
                        )
                    }
                }
            } else {
                // LAYOUT MOBILE
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                CardTitle("Beneficiário")
                                Spacer(Modifier.height(8.dp))

                                ExposedDropdownMenuBox(
                                    expanded = isBeneficiaryDropdownExpanded,
                                    onExpandedChange = {
                                        isBeneficiaryDropdownExpanded = it
                                        if(it) beneficiaryFocusRequester.requestFocus()
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = beneficiaryQuery,
                                        onValueChange = {
                                            beneficiaryQuery = it
                                            if (selectedBeneficiary != null && it != selectedBeneficiary?.name) {
                                                selectedBeneficiary = null
                                            }
                                            beneficiaryFocusRequester.requestFocus()
                                        },
                                        label = { Text("Pesquisar Beneficiário") },
                                        placeholder = { Text("Digite o nome...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .focusRequester(beneficiaryFocusRequester),
                                        trailingIcon = {
                                            if (isBeneficiaryLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                                            }
                                        },
                                        singleLine = true,
                                        isError = (beneficiaryQuery.isNotEmpty() && selectedBeneficiary == null) || deliveryUiState.errors.containsKey("beneficiary")
                                    )

                                    DropdownMenu(
                                        expanded = isBeneficiaryDropdownExpanded,
                                        onDismissRequest = { isBeneficiaryDropdownExpanded = false },
                                        modifier = Modifier
                                            .exposedDropdownSize(true)
                                            .background(MaterialTheme.colorScheme.surface),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        val list = beneficiarySearchResults?.data ?: emptyList()
                                        if (list.isEmpty() && !isBeneficiaryLoading) {
                                            DropdownMenuItem(text = { Text("Sem resultados") }, onClick = {}, enabled = false)
                                        } else {
                                            list.forEach { beneficiary ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(beneficiary.name, style = MaterialTheme.typography.bodyLarge)
                                                            Text(beneficiary.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedBeneficiary = beneficiary
                                                        beneficiaryQuery = beneficiary.name
                                                        isBeneficiaryDropdownExpanded = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                CardTitle("Adicionar Produto")
                                Spacer(Modifier.height(8.dp))

                                ExposedDropdownMenuBox(
                                    expanded = isProductDropdownExpanded,
                                    onExpandedChange = {
                                        isProductDropdownExpanded = it
                                        if(it) productFocusRequester.requestFocus()
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = productQuery,
                                        onValueChange = {
                                            productQuery = it
                                            productFocusRequester.requestFocus()
                                        },
                                        label = { Text("Produto") },
                                        placeholder = { Text("Nome ou Código de Barras") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .focusRequester(productFocusRequester),
                                        trailingIcon = {
                                            if (isProductSearchLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (productQuery.isNotEmpty()) {
                                                    barcode = productQuery
                                                    isProductDropdownExpanded = false
                                                }
                                            }
                                        )
                                    )

                                    DropdownMenu(
                                        expanded = isProductDropdownExpanded,
                                        onDismissRequest = { isProductDropdownExpanded = false },
                                        modifier = Modifier
                                            .exposedDropdownSize(true)
                                            .background(MaterialTheme.colorScheme.surface),
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        val list = productSearchResults
                                        if (list.isEmpty() && !isProductSearchLoading) {
                                            DropdownMenuItem(text = { Text("Não encontrado") }, onClick = {}, enabled = false)
                                        } else {
                                            list.forEach { product ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(text = product.name ?: "Sem Nome", style = MaterialTheme.typography.bodyLarge)
                                                            Text(text = product.barcode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        barcode = product.barcode
                                                        isProductDropdownExpanded = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(productsToDeliver) { index, product ->
                        DeliveryProductCard(
                            product = product,
                            index = index,
                            onRemove = { productsToDeliver = productsToDeliver.toMutableList().apply { removeAt(index) } },
                            onExpand = {
                                val updated = productsToDeliver.toMutableList()
                                updated[index] = updated[index].copy(isExpanded = !updated[index].isExpanded)
                                productsToDeliver = updated
                            },
                            onUpdateQuantity = { newTotal ->
                                val isValid = newTotal <= product.totalStock
                                val newLots = recalculateLotsFIFO(newTotal, product.availableLots, product.barcode)
                                val updatedList = productsToDeliver.toMutableList()
                                updatedList[index] = product.copy(quantityToDeliver = newTotal, selectedLots = newLots, hasError = !isValid)
                                productsToDeliver = updatedList
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
                            enabled = !deliveryUiState.isLoading,
                            onClick = {
                                deliveriesViewModel.scheduleDelivery(
                                    beneficiaryId = selectedBeneficiary?.beneficiaryId,
                                    scheduledDate = scheduledDate,
                                    isScheduled = isScheduled,
                                    products = productsToDeliver,
                                    note = deliveryNote
                                )
                            },
                            isLoading = deliveryUiState.isLoading
                        )
                    }
                }
            }

            if (isLoadingProduct || deliveryUiState.isLoading) LoadingWidget()
        }
    }
}

// --- COMPONENTES REUTILIZÁVEIS ---

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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            CardTitle("Detalhes")
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                ValidatedDateField(
                    value = scheduledDate?.format(formatter) ?: "",
                    onValueChange = { newDateStr ->
                        if (newDateStr.isNotBlank()) {
                            try {
                                val date = LocalDate.parse(newDateStr, formatter)
                                onDateChange(date)
                            } catch (e: Exception) {
                                // Ignorar erro
                            }
                        }
                    },
                    label = "Data da Entrega",
                    error = dateError,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = deliveryNote, onValueChange = onNoteChange,
                label = { Text("Observações") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                maxLines = 3
            )
        }
    }
}

@Composable
fun SubmitButton(enabled: Boolean, onClick: () -> Unit, isLoading: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("Agendar Entrega", fontSize = 16.sp)
        }
    }
}

@Composable
fun DeliveryProductsListSection(
    products: List<DeliveryProductToSend>,
    onProductRemove: (Int) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onProductExpanded: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        onUpdateQuantity = { qty -> onUpdateQuantity(index, qty) }
                    )
                }
            }
        }
    }
}
@Composable
fun DeliveryProductCard(
    product: DeliveryProductToSend,
    index: Int,
    onRemove: () -> Unit,
    onExpand: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
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
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Qtd.: ${product.quantityToDeliver} | Total: ${product.totalStock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    Icon(
                        imageVector = if (product.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Remover",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(visible = product.isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        "Quantidade a Exportar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = if (product.quantityToDeliver == 0) "" else product.quantityToDeliver.toString(),
                        onValueChange = {
                            val newValue = it.toIntOrNull() ?: 0
                            onUpdateQuantity(newValue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Total") },
                        isError = product.hasError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            if (product.hasError) Text("Quantidade excede o stock disponível (${product.totalStock})")
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    if (product.selectedLots.isNotEmpty()) {
                        Text(
                            "Distribuição por Lotes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))

                        product.selectedLots.forEach { item ->
                            val lotInfo = product.availableLots.find { it.lot == item.lot }

                            // LÓGICA DE FORMATAÇÃO DA DATA (dd/MM/yyyy)
                            val formattedDate = lotInfo?.expiryDate?.let { date ->
                                "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
                            } ?: "N/A"

                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    // Alterado aqui para usar a data formatada
                                    "Lote ${item.lot} | Validade: $formattedDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("${item.quantity} uni.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}