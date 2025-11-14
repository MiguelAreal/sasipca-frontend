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

@Composable
actual fun BarcodeInputField(
    barcode: String,
    onBarcodeScanned: (String) -> Unit
) {
    OutlinedTextField(
        value = barcode,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            onBarcodeScanned(digitsOnly)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Ex.: 7898765430018", color = Color(0xFF999999)) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
