package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import sasipca.models.ProductGroup
import sasipca.models.SnackbarType
import sasipca.network.ApiClient
import sasipca.repositories.AdjustmentRepository
import sasipca.repositories.OFFRepository
import sasipca.repositories.ProductRepository
import sasipca.ui.components.BarcodeInputField
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.theme.CardTitle
import sasipca.utils.SnackbarManager
import sasipca.viewmodels.ProductViewModel
import sasipca.viewmodels.StockAdjustmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    adjustmentRepository: AdjustmentRepository,
    productRepository: ProductRepository
) {
    val focusManager = LocalFocusManager.current
    val navigator = LocalNavigator.currentOrThrow

    // Inicializar ViewModels
    val viewModel = remember { StockAdjustmentViewModel(adjustmentRepository) }
    val productViewModel = remember { ProductViewModel(productRepository) }
    val offRepository = remember { OFFRepository(ApiClient.client) }

    // Estados UI
    val uiState by viewModel.uiState.collectAsState()

    // Ler estados do ProductViewModel (MutableState)
    val productDetail = productViewModel.selectedProductDetail
    val isProductLoading = productViewModel.isLoading
    val productError = productViewModel.errorMessage

    // Campos do Formulário
    var barcode by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<ProductGroup?>(null) }
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }

    // Estados de Pesquisa
    var productQuery by remember { mutableStateOf("") }
    val productSearchResults = productViewModel.filteredItems // Lista para o Autocomplete

    // Estado dropdown Grupos
    var isGroupDropdownExpanded by remember { mutableStateOf(false) }

    // --- 1. Feedback UI (Sucesso/Erro do Ajuste) ---
    LaunchedEffect(uiState) {
        if (uiState.success) {
            viewModel.clearState()
            navigator.pop()
        }
        if (uiState.errorMessage != null) {
            SnackbarManager.show(uiState.errorMessage!!, SnackbarType.ERROR)
            viewModel.clearState()
        }
    }

    // --- 2. Feedback UI (Erro ao carregar produto) ---
    LaunchedEffect(productError) {
        if (productError != null) {
            SnackbarManager.show("Erro produto: $productError", SnackbarType.ERROR)
            barcode = ""
        }
    }

    // --- 3. Lógica Pesquisa Produto (Autocomplete) ---
    LaunchedEffect(productQuery) {
        if (productQuery.isEmpty()) return@LaunchedEffect

        delay(400) // Debounce

        // Se for só números e comprido, assume scan manual
        val isPotentialBarcode = productQuery.all { it.isDigit() } && productQuery.length >= 8
        if (isPotentialBarcode) {
            barcode = productQuery
        } else {
            productViewModel.loadProducts(search = productQuery)
        }
    }

    // --- 4. Carregar Detalhe do Produto ---
    LaunchedEffect(barcode) {
        if (barcode.isNotEmpty()) {
            productViewModel.getProduct(barcode, offRepository)
            selectedGroup = null
        } else {
            productViewModel.resetProduct()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(title = "Ajuste de Stock")

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // === CARD 1: SELEÇÃO DO PRODUTO (REFEITO) ===
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CardTitle("Produto")
                        Spacer(Modifier.height(8.dp))

                        // --- COMPONENTE CENTRALIZADO ---
                        BarcodeInputField(
                            value = productQuery,
                            onValueChange = {
                                productQuery = it
                                if (it.isEmpty()) barcode = "" // Reset se limpar
                            },
                            suggestions = productSearchResults,
                            onSuggestionSelected = { product ->
                                barcode = product.barcode
                                productQuery = product.name ?: product.barcode
                                focusManager.clearFocus()
                            },
                            // O componente BarcodeInputField já trata do Scanner (Android)
                            // e do Dropdown (Desktop/Android) internamente.
                        )
                        // -------------------------------

                        if (productDetail != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Selecionado: ${productDetail.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // === CARD 2: SELEÇÃO DO LOTE ===
                if (productDetail != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CardTitle("Selecionar Lote")
                            Spacer(Modifier.height(8.dp))

                            val groups = productDetail.productGroups ?: emptyList()

                            if (groups.isEmpty()) {
                                Text("Este produto não tem lotes em stock.", color = MaterialTheme.colorScheme.error)
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = isGroupDropdownExpanded,
                                    onExpandedChange = { isGroupDropdownExpanded = it }
                                ) {
                                    val groupText = selectedGroup?.let {
                                        // Formatar Data (dd/MM/yyyy)
                                        val date = it.expiryDate
                                        val dateStr = "${date.dayOfMonth.toString().padStart(2,'0')}/${date.monthNumber.toString().padStart(2,'0')}/${date.year}"
                                        "Validade: $dateStr (Disp: ${it.availableStock})"
                                    } ?: "Selecione um Grupo..."

                                    OutlinedTextField(
                                        value = groupText,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Lote / Validade") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupDropdownExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isGroupDropdownExpanded,
                                        onDismissRequest = { isGroupDropdownExpanded = false }
                                    ) {
                                        groups.forEach { group ->
                                            val date = group.expiryDate
                                            val dateStr = "${date.dayOfMonth.toString().padStart(2,'0')}/${date.monthNumber.toString().padStart(2,'0')}/${date.year}"

                                            DropdownMenuItem(
                                                text = {
                                                    Text("Val: $dateStr - Disp: ${group.availableStock}")
                                                },
                                                onClick = {
                                                    selectedGroup = group
                                                    isGroupDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // === CARD 3: DETALHES E AÇÃO ===
                if (selectedGroup != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CardTitle("Detalhes do Ajuste")
                            Spacer(Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { isAddition = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAddition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isAddition) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) { Text("Adicionar (+)") }
                                Button(
                                    onClick = { isAddition = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isAddition) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (!isAddition) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) { Text("Remover (-)") }
                            }

                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                                label = { Text("Quantidade") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                suffix = { Text("un") },
                                supportingText = {
                                    if (!isAddition) {
                                        Text("Disponível para remover: ${selectedGroup?.availableStock}")
                                    }
                                }
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                label = { Text("Justificação (Obrigatório)") },
                                placeholder = { Text("Ex: Quebra, Inventário anual...") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                maxLines = 4
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.submitAdjustment(
                                barcode = barcode,
                                selectedGroup = selectedGroup,
                                quantityStr = quantity,
                                isAddition = isAddition,
                                note = note
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Confirmar Ajuste", fontSize = 16.sp)
                        }
                    }
                }
            }

            if (isProductLoading || uiState.isLoading) LoadingWidget()
        }
    }
}