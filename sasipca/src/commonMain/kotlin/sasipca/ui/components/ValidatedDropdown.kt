package sasipca.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sasipca.ui.theme.UnderlineError

interface NamedItem {
    val name: String
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : NamedItem> ValidatedDropdown(
    modifier: Modifier = Modifier,
    label: String,
    items: List<T>,
    selectedItem: T?,
    onSelect: (T?) -> Unit,
    error: String? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) {
                    expanded = it
                }
            }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth(),
                value = selectedItem?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                singleLine = true,
                minLines = 1,
                maxLines = 1,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ícone do dropdown (seta)
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        if (selectedItem != null && enabled) {
                            IconButton(onClick = { onSelect(null) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar seleção"
                                )
                            }
                        }
                    }
                },
                isError = error != null,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    cursorColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            )

            // Só renderiza o menu se ativado (segurança extra)
            if (enabled) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                onSelect(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (error != null) {
            UnderlineError(error)
        }
    }
}