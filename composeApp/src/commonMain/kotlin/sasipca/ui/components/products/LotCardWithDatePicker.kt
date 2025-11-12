package sasipca.ui.components.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.LotToEnter


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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Reduced elevation for a flatter look
        shape = RoundedCornerShape(8.dp), // Slightly smaller radius
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // COR DO SETTINGSSCREEN
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Reduced padding inside the card
            verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lote ${index + 1}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        // NOVO: Aplicar cor vermelha fixa (MaterialTheme.colorScheme.error)
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Remover lote",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Horizontal layout for Lot Number and Quantity to save vertical space
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = lot.lotNumber,
                    onValueChange = { onLotChange(lot.copy(lotNumber = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Nº Lote") }, // Shorter label
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = lot.quantity,
                    onValueChange = { onLotChange(lot.copy(quantity = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Quant.") }, // Shorter label
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Date Picker Field (Full width)
            OutlinedTextField(
                value = lot.expirationDate,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Data de Validade") },
                readOnly = true,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Selecionar data"
                        )
                    }
                }
            )
        }
    }

    // DatePickerDialog remains the same
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(millis))
                            onLotChange(lot.copy(expirationDate = date))
                        }
                        showDatePicker = false
                    }
                ) {
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
}