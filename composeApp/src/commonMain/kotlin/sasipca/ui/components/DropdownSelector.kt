package sasipca.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

interface NamedItem {
    val name: String
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : NamedItem> DropdownSelector(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        // CORREÇÃO: Usar ExposedDropdownMenuDefaults.TextFieldDecorationBox para imitar o estilo OutlinedTextField
        OutlinedTextField(
            // O componente que atua como âncora do menu
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            value = selectedItem?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(8.dp)
        )
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