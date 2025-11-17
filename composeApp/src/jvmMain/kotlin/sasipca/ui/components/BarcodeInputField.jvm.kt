package sasipca.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import sasipca.ui.theme.UnderlineError

@Composable
actual fun BarcodeInputField(
    barcode: String,
    onBarcodeScanned: (String) -> Unit,
    error: String?,
) {
    OutlinedTextField(
        value = barcode,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            onBarcodeScanned(digitsOnly)
        },
        modifier = Modifier
            .fillMaxWidth(),
        placeholder = { Text("Ex.: 7898765430018") },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            cursorColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    )
    if (error != null) {
        UnderlineError(error)
    }
}
