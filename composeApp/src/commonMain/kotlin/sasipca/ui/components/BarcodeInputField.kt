package sasipca.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import sasipca.models.Product

@Composable
expect fun BarcodeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Produto",
    placeholder: String = "Nome ou Código de Barras",
    error: String? = null,
    suggestions: List<Product> = emptyList(), // Lista filtrada do ViewModel
    onSuggestionSelected: (Product) -> Unit, // Callback ao clicar num item
    modifier: Modifier = Modifier
)