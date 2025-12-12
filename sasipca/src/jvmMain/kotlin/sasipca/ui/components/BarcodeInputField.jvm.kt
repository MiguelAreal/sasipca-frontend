package sasipca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import sasipca.models.Product
import sasipca.ui.theme.UnderlineError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun BarcodeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String?,
    suggestions: List<Product>,
    onSuggestionSelected: (Product) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Abre o dropdown se houver texto e sugestões
    LaunchedEffect(value, suggestions) {
        expanded = value.isNotEmpty() && suggestions.isNotEmpty()
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { expanded = false }),
                shape = RoundedCornerShape(8.dp),
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )

            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    suggestions.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = product.name ?: "Sem Nome",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = product.barcode,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onSuggestionSelected(product)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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