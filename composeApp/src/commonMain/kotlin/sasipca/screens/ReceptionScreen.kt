package sasipca.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import sasipca.ApiClient
import sasipca.repositories.OFFRepository
import sasipca.ui.components.BarcodeInputField
import sasipca.ui.components.DropdownSelector
import sasipca.ui.components.Header
import kotlinx.coroutines.launch
import sasipca.models.Category
import sasipca.models.LotToEnter
import sasipca.models.UnitType
import sasipca.ui.components.NamedItem
import sasipca.ui.components.products.LotCardWithDatePicker
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen() {
    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUnit by remember { mutableStateOf<UnitType?>(null) }
    var unitSize by remember { mutableStateOf("") }
    var unitType by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var lots by remember { mutableStateOf(listOf(LotToEnter("", "", ""))) }

    var isLoading by remember { mutableStateOf(false) }
    var lotsExpanded by remember { mutableStateOf(true) }


    // Dummies, assume they come from a ViewModel or similar
    val categories = listOf(
        Category(1, "Alimentos"),
        Category(2, "Higiene")
    )

    val units = listOf(
        UnitType(1, "Kg"),
        UnitType(2, "L"),
        UnitType(3, "ml"),
        UnitType(4, "Uni")
    )

    val offRepository = remember { OFFRepository(ApiClient.client) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(barcode) {
        // Reset product info when barcode changes
        if (barcode.length > 0) {
            productName = ""
            imageUrl = null
            selectedCategory = null
            selectedUnit = null
            unitSize = ""

            if (barcode.length >= 8) {
                isLoading = true
                scope.launch {
                    try {
                        val productResponse = offRepository.getProductByBarcode(barcode)
                        val product = productResponse?.product

                        if (product != null) {
                            productName = product.product_name ?: ""
                            imageUrl = product.image_url
                            unitSize = product.product_quantity?.toString() ?: ""
                            unitType = product.product_quantity_unit ?: ""

                            selectedUnit = units.find {
                                it.name.equals(unitType, ignoreCase = true)
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth >= 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Header(title = "Receção de Stock")

            Box(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    // CÓDIGO DO MODO ECRÃ GRANDE (DESKTOP)
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
                            // Section 1: Barcode (Fixed height)
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
                                    Text(
                                        "Código de Barras",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    BarcodeInputField(
                                        barcode = barcode,
                                        onBarcodeScanned = { barcode = it }
                                    )
                                }
                            }

                            // Section 2: Product Details and Notes (Takes remaining vertical space)
                            ProductInfoSection(
                                productName = productName,
                                onProductNameChange = { productName = it },
                                imageUrl = imageUrl,
                                selectedCategory = selectedCategory,
                                onCategorySelect = { selectedCategory = it },
                                categories = categories,
                                selectedUnit = selectedUnit,
                                onUnitSelect = { selectedUnit = it },
                                units = units,
                                unitSize = unitSize,
                                onUnitSizeChange = { unitSize = it },
                                note = note,
                                onNoteChange = { note = it },
                                isWideScreen = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Right side - Lots and Register Button (1/3 width)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            LotsSection(
                                lots = lots,
                                onAddLot = { lots = lots + LotToEnter("", "", "") },
                                onLotChange = { index, updatedLot ->
                                    lots = lots.toMutableList().apply { set(index, updatedLot) }
                                },
                                onRemoveLot = { index ->
                                    if (lots.size > 1) {
                                        lots = lots.toMutableList().apply { removeAt(index) }
                                    }
                                },
                                onSubmit = { /* TODO: Submeter */ },
                                isWideScreen = true
                            )
                        }
                    }
                } else {
                    // CÓDIGO DO MODO ECRÃ ESTREITO (COMPACTO/MÓVEL)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        // Barcode field for narrow screen
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
                                    Text(
                                        "Código de Barras",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    BarcodeInputField(
                                        barcode = barcode,
                                        onBarcodeScanned = { barcode = it }
                                    )
                                }
                            }
                        }

                        // Product Info Section for narrow screen
                        item {
                            ProductInfoSection(
                                productName = productName,
                                onProductNameChange = { productName = it },
                                imageUrl = imageUrl,
                                selectedCategory = selectedCategory,
                                onCategorySelect = { selectedCategory = it },
                                categories = categories,
                                selectedUnit = selectedUnit,
                                onUnitSelect = { selectedUnit = it },
                                units = units,
                                unitSize = unitSize,
                                onUnitSizeChange = { unitSize = it },
                                note = note,
                                onNoteChange = { note = it },
                                isWideScreen = false
                            )
                        }


                        // Lotes Colapsáveis (Header)
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
                                        Text(
                                            "Lotes (${lots.size})",
                                            fontSize = 16.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            imageVector = if (lotsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (lotsExpanded) "Recolher lotes" else "Expandir lotes",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { lots = lots + LotToEnter("", "", "") }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Add,
                                            contentDescription = "Adicionar lote"
                                        )
                                    }
                                }
                            }
                        }

                        // Lotes Colapsáveis (Content)
                        item {
                            // CORREÇÃO: Envolver AnimatedVisibility numa Column explícita para resolver o problema do receiver.
                            Column {
                                AnimatedVisibility(
                                    visible = lotsExpanded,
                                    enter = expandVertically(expandFrom = Alignment.Top),
                                    exit = shrinkVertically(shrinkTowards = Alignment.Top)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        lots.forEachIndexed { index, lot ->
                                            LotCardWithDatePicker(
                                                lot = lot,
                                                index = index,
                                                onLotChange = { updatedLot ->
                                                    lots = lots.toMutableList().apply { set(index, updatedLot) }
                                                },
                                                onRemove = {
                                                    if (lots.size > 1) {
                                                        lots = lots.toMutableList().apply { removeAt(index) }
                                                    }
                                                },
                                                canRemove = lots.size > 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Botão de submissão
                        item {
                            Button(
                                onClick = { /* TODO: Submeter */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Registar Receção",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

// --- ProductInfoSection (Cores aplicadas) ---

@Composable
fun ProductInfoSection(
    productName: String,
    onProductNameChange: (String) -> Unit,
    imageUrl: String?,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit,
    categories: List<Category>,
    selectedUnit: UnitType?,
    onUnitSelect: (UnitType?) -> Unit,
    units: List<UnitType>,
    unitSize: String,
    onUnitSizeChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (isWideScreen) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Informações do Produto",
                        fontSize = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (imageUrl != null) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Imagem do produto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(
                            modifier = if (imageUrl != null) Modifier.weight(2f) else Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = productName,
                                onValueChange = onProductNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nome do Produto") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            DropdownSelector(
                                label = "Categoria",
                                items = categories,
                                selectedItem = selectedCategory,
                                onSelect = onCategorySelect,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DropdownSelector(
                                    label = "Unidade",
                                    items = units,
                                    selectedItem = selectedUnit,
                                    onSelect = onUnitSelect,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = unitSize,
                                    onValueChange = onUnitSizeChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Quantidade") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            "Observações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = onNoteChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Adicione notas sobre o produto...") },
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 5
                        )
                    }
                }
            }
        }
    } else {
        // Narrow Screen Layout (Cores aplicadas)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Informações do Produto",
                        fontSize = 16.sp
                    )

                    if (imageUrl != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Imagem do produto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    OutlinedTextField(
                        value = productName,
                        onValueChange = onProductNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nome do Produto") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    DropdownSelector(
                        label = "Categoria",
                        items = categories,
                        selectedItem = selectedCategory,
                        onSelect = onCategorySelect,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DropdownSelector(
                            label = "Unidade",
                            items = units,
                            selectedItem = selectedUnit,
                            onSelect = onUnitSelect,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = unitSize,
                            onValueChange = onUnitSizeChange,
                            modifier = Modifier.weight(1f),
                            label = { Text("Quantidade") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Notes
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
                    Text(
                        "Observações",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Adicione notas sobre o produto...") },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 5
                    )
                }
            }
        }
    }
}

// --- LotsSection (Cores aplicadas) ---

@Composable
fun LotsSection(
    lots: List<LotToEnter>,
    onAddLot: () -> Unit,
    onLotChange: (Int, LotToEnter) -> Unit,
    onRemoveLot: (Int) -> Unit,
    onSubmit: () -> Unit,
    isWideScreen: Boolean // New parameter
) {
    if (isWideScreen) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Lots Card (Takes all available space, except for the submit button)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes remaining space
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Lotes",
                            fontSize = 16.sp
                        )
                        IconButton(onClick = onAddLot) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Adicionar lote"
                            )
                        }
                    }

                    // Lots List (Optimized for height - less padding)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes remaining height in the Card
                            .padding(horizontal = 8.dp), // Reduced horizontal padding
                        verticalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
                        contentPadding = PaddingValues(bottom = 8.dp) // Added bottom padding for last item
                    ) {
                        itemsIndexed(lots) { index, lot ->
                            LotCardWithDatePicker(
                                lot = lot,
                                index = index,
                                onLotChange = { onLotChange(index, it) },
                                onRemove = { onRemoveLot(index) },
                                canRemove = lots.size > 1
                            )
                        }
                    }
                }
            }

            // Submit Button (Outside the Card, aligned right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End // Aligns button to the right
            ) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .widthIn(min = 200.dp) // Fixed minimum width for better appearance
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Registar Receção",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
