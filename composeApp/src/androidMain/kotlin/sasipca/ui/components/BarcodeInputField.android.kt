package sasipca.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun BarcodeInputField(
    barcode: String,
    onBarcodeScanned: (String) -> Unit
) {
    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (granted) showScanner = true }
    )

    // Campo de texto normal
    OutlinedTextField(
        value = barcode,
        onValueChange = { onBarcodeScanned(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Ex.: 7898765430018", color = Color(0xFF999999)) },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ->
                        showScanner = true
                    else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Icon(Icons.Default.Scanner, contentDescription = "Ler código de barras")
            }
        }
    )

    // Overlay com o scanner em Dialog (50% da tela)
    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.5f)
                    .background(Color.Black)
            ) {
                BarcodeScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeDetected = { code ->
                        onBarcodeScanned(code)
                        showScanner = false
                    }
                )

                // Moldura de guia para centralizar o código de barras
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f)
                        .align(Alignment.Center)
                        .background(Color.Transparent)
                        .padding(2.dp)
                ) {
                    // Bordas da moldura
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 4.dp.toPx()
                        val cornerLength = 40.dp.toPx()

                        // Cantos da moldura
                        drawLine(Color.White, Offset(0f, 0f),
                            Offset(cornerLength, 0f), strokeWidth)
                        drawLine(Color.White, Offset(0f, 0f),
                            Offset(0f, cornerLength), strokeWidth)

                        drawLine(Color.White, Offset(size.width, 0f),
                            Offset(size.width - cornerLength, 0f), strokeWidth)
                        drawLine(Color.White, Offset(size.width, 0f),
                            Offset(size.width, cornerLength), strokeWidth)

                        drawLine(Color.White, Offset(0f, size.height),
                            Offset(cornerLength, size.height), strokeWidth)
                        drawLine(Color.White, Offset(0f, size.height),
                            Offset(0f, size.height - cornerLength), strokeWidth)

                        drawLine(Color.White, Offset(size.width, size.height),
                            Offset(size.width - cornerLength, size.height), strokeWidth)
                        drawLine(Color.White, Offset(size.width, size.height),
                            Offset(size.width, size.height - cornerLength), strokeWidth)
                    }
                }

                // Texto de instrução
                Text(
                    text = "Aponte para o código de barras",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}