package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import g8.ipca.sasipca.sasipca.models.*
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.repositories.OFFRepository
import g8.ipca.sasipca.sasipca.ui.components.BarcodeInputField
import g8.ipca.sasipca.sasipca.ui.components.DropdownSelector
import g8.ipca.sasipca.sasipca.ui.components.Header
import g8.ipca.sasipca.sasipca.ui.components.LotCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen() {
    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUnit by remember { mutableStateOf<UnitType?>(null) }
    var unitSize by remember { mutableStateOf("") }
    var unitType by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var lots by remember { mutableStateOf(listOf(LotToEnter("", "", ""))) }

    var isLoading by remember { mutableStateOf(false) }

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

    /**
     * Quando o barcode muda:
     * - limpa os campos do produto
     * - se tiver 8+ dígitos, faz a query ao OpenFoodFacts
     */
    LaunchedEffect(barcode) {
        // Limpar os campos
        productName = ""
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(title = "Receção de Stock")

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {

                /** Código de Barras **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Código de Barras",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            BarcodeInputField(
                                barcode = barcode,
                                onBarcodeScanned = { barcode = it }
                            )
                        }
                    }
                }

                /** Informações do Produto **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Informações do Produto",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = productName,
                                onValueChange = { productName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nome do Produto") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            DropdownSelector(
                                label = "Categoria",
                                items = categories,
                                selectedItem = selectedCategory,
                                onSelect = { selectedCategory = it },
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
                                    onSelect = { selectedUnit = it },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = unitSize,
                                    onValueChange = { unitSize = it },
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
                }

                /** Lotes **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    lots = lots + LotToEnter("", "", "")
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = "Adicionar lote",
                                    tint = Color(0xFF3D4A7A)
                                )
                            }
                        }
                    }
                }

                /** Lista de Lotes **/
                itemsIndexed(lots) { index, lot ->
                    LotCard(
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

                /** Observações **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Observações",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
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

                /** Botão de Submeter **/
                item {
                    Button(
                        onClick = { /* TODO: Submeter */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D4A7A)
                        )
                    ) {
                        Text(
                            "Registar Receção",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            /** Indicador de Carregamento **/
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
