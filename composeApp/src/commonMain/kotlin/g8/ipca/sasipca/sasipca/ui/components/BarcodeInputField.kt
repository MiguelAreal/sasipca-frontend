package g8.ipca.sasipca.sasipca.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun BarcodeInputField(
    barcode: String,
    onBarcodeScanned: (String) -> Unit
)
