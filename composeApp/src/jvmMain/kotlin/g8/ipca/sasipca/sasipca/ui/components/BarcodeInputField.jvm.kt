package g8.ipca.sasipca.sasipca.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun BarcodeInputField(
    barcode: String,
    onBarcodeScanned: (String) -> Unit
) {
    OutlinedTextField(
        value = barcode,
        onValueChange = { onBarcodeScanned(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        label = { Text("Código de barras") },
        placeholder = { Text("Insira manualmente") },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8F8F8),
            unfocusedContainerColor = Color(0xFFF8F8F8),
            focusedBorderColor = Color(0xFF3D4A7A),
            unfocusedBorderColor = Color(0xFFE0E0E0)
        )
    )
}
