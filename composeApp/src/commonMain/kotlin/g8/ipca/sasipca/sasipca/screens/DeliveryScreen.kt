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
import java.time.Instant
import java.time.ZoneId

data class DeliveryItemToAdd(
    val barcode: String,
    val lot: String,
    val quantity: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen() {
    var selectedBeneficiary by remember { mutableStateOf<Beneficiary?>(null) }
    var isImmediateDelivery by remember { mutableStateOf(true) }
    var scheduledDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf(DeliveryItemToAdd("", "", ""))) }

    var expandedBeneficiary by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Lista de beneficiários - substituir pela chamada à API
    val beneficiaries = listOf(
        Beneficiary(1, "João Silva", "123456789"),
        Beneficiary(2, "Maria Santos", "987654321")
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HeaderSection("Entrega de Stock")

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            scheduledDate = "%02d/%02d/%04d".format(
                                date.dayOfMonth,
                                date.monthValue,
                                date.year
                            )
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Tipo de Entrega
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
                            text = "Tipo de Entrega",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isImmediateDelivery) "Entrega Imediata" else "Agendar Entrega",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isImmediateDelivery)
                                        "Stock será deduzido imediatamente"
                                    else
                                        "Stock será reservado para a data",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = !isImmediateDelivery,
                                onCheckedChange = {
                                    isImmediateDelivery = !it
                                    if (isImmediateDelivery) {
                                        scheduledDate = ""
                                    }
                                }
                            )
                        }

                        // Data Agendada (só aparece se não for imediato)
                        if (!isImmediateDelivery) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Data Agendada",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = scheduledDate,
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                placeholder = { Text("Selecione a data") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Calendário",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                readOnly = true,
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Beneficiário
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
                            text = "Beneficiário",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedBeneficiary,
                            onExpandedChange = { expandedBeneficiary = it }
                        ) {
                            OutlinedTextField(
                                value = selectedBeneficiary?.name ?: "",
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                placeholder = { Text("Selecione o beneficiário") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedBeneficiary
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBeneficiary,
                                onDismissRequest = { expandedBeneficiary = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                beneficiaries.forEach { beneficiary ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = beneficiary.name,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "NIF: ${beneficiary.nif}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedBeneficiary = beneficiary
                                            expandedBeneficiary = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Produtos a Entregar
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
                                text = "Produtos a Entregar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = {
                                    items = items + DeliveryItemToAdd("", "", "")
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = "Adicionar produto",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Lista de Produtos
            itemsIndexed(items) { index, item ->
                DeliveryItemCard(
                    item = item,
                    index = index,
                    onItemChange = { updatedItem ->
                        items = items.toMutableList().apply { set(index, updatedItem) }
                    },
                    onRemove = {
                        if (items.size > 1) {
                            items = items.toMutableList().apply { removeAt(index) }
                        }
                    },
                    canRemove = items.size > 1
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
                            placeholder = { Text("Adicione notas sobre a entrega...") },
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
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isImmediateDelivery)
                                Icons.Default.Check
                            else
                                Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isImmediateDelivery)
                                "Registar Entrega Imediata"
                            else
                                "Agendar Entrega",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeliveryItemCard(
    item: DeliveryItemToAdd,
    index: Int,
    onItemChange: (DeliveryItemToAdd) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produto ${index + 1}",
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
                            contentDescription = "Remover produto",
                            tint = Color(0xFFE74C3C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Código de Barras
            Column {
                Text(
                    text = "Código de Barras",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                BarcodeInputField(
                    barcode = item.barcode,
                    onBarcodeScanned = { onItemChange(item.copy(barcode = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lote
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lote",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = item.lot,
                        onValueChange = { onItemChange(item.copy(lot = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("LOT123") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Quantidade
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quantidade",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = item.quantity,
                        onValueChange = { onItemChange(item.copy(quantity = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}

// Data classes auxiliares (adicionar ao teu projeto)
data class Beneficiary(
    val id: Int,
    val name: String,
    val nif: String
)