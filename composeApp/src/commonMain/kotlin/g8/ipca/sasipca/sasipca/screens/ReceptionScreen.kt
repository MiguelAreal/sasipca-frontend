package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import g8.ipca.sasipca.sasipca.ui.components.BarcodeInputField
import g8.ipca.sasipca.sasipca.ui.components.HeaderSection
import g8.ipca.sasipca.sasipca.ui.utils.getFormattedDatePt

data class LotToEnter(
    val lot: String,
    val quantity: String,
    val expiryDate: String
)

data class Category(val id: Int, val name: String)
data class UnitType(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen() {
    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUnit by remember { mutableStateOf<UnitType?>(null) }
    var unitSize by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var lots by remember { mutableStateOf(listOf(LotToEnter("", "", ""))) }

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    val categories = listOf(
        Category(1, "Alimentos"),
        Category(2, "Higiene")
    )

    val units = listOf(
        UnitType(1, "Kg"),
        UnitType(2, "L"),
        UnitType(3, "Uni")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HeaderSection("Receção", getFormattedDatePt())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Código de Barras
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Código de Barras",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        BarcodeInputField(
                            barcode = barcode,
                            onBarcodeScanned = { barcode = it }
                        )
                    }
                }
            }

            // Informações do Produto
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Informações do Produto",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Nome do Produto
                        Column {
                            Text(
                                text = "Nome do Produto",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = productName,
                                onValueChange = { productName = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ex: Arroz Cigala") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        // Categoria
                        Column {
                            Text(
                                text = "Categoria",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = expandedCategory,
                                onExpandedChange = { expandedCategory = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "",
                                    onValueChange = {},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    readOnly = true,
                                    placeholder = { Text("Selecione a categoria") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCategory,
                                    onDismissRequest = { expandedCategory = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                selectedCategory = category
                                                expandedCategory = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Unidade
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unidade",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = expandedUnit,
                                    onExpandedChange = { expandedUnit = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedUnit?.name ?: "",
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        readOnly = true,
                                        placeholder = { Text("Un.") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedUnit,
                                        onDismissRequest = { expandedUnit = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        units.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit.name) },
                                                onClick = {
                                                    selectedUnit = unit
                                                    expandedUnit = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Tamanho da Unidade
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tamanho",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = unitSize,
                                    onValueChange = { unitSize = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Ex: 2") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Lotes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lotes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
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
            }

            // Lista de Lotes
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

            // Observações
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Observações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
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

            // Botão de Submeter
            item {
                Button(
                    onClick = { /* TODO: Submit */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Registar Receção",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotCard(
    lot: LotToEnter,
    index: Int,
    onLotChange: (LotToEnter) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mostrar DatePickerDialog se showDatePicker for true
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                                val formatted = "%02d/%02d/%04d".format(date.dayOfMonth, date.monthValue, date.year)
                                onLotChange(lot.copy(expiryDate = formatted))
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lote ${index + 1}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Remover lote",
                            tint = Color(0xFFE74C3C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Número do Lote
            Column {
                Text(
                    text = "Número do Lote",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = lot.lot,
                    onValueChange = { onLotChange(lot.copy(lot = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: LOT2025001") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quantidade
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quantidade",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = lot.quantity,
                        onValueChange = { onLotChange(lot.copy(quantity = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Data de Validade
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Validade",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Data de Validade
                    OutlinedTextField(
                        value = lot.expiryDate,
                        onValueChange = {},
                        modifier = Modifier
                            .clickable { showDatePicker = true },
                        placeholder = { Text("DD/MM/AAAA") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Calendário",
                                tint = Color(0xFF3D4A7A),
                                modifier = Modifier.size(20.dp)
                            )
                        },

                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}