package sasipca.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
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
    var showScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (granted) showScanner = true }
    )

    LaunchedEffect(value, suggestions, showScanner) {
        expanded = if (showScanner) {
            false
        } else {
            value.isNotEmpty() && suggestions.isNotEmpty()
        }
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
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    IconButton(onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ->
                                showScanner = true
                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Ler código de barras",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    expanded = false
                    focusManager.clearFocus()
                }),
                shape = RoundedCornerShape(8.dp),
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )

            // CORREÇÃO: Usar DropdownMenu padrão em vez de ExposedDropdownMenu
            if (suggestions.isNotEmpty()) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .exposedDropdownSize(true), // Importante: Mantém a largura igual ao TextField
                    properties = PopupProperties(focusable = false) // Agora funciona aqui!
                ) {
                    suggestions.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
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
                                focusManager.clearFocus()
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

    // --- SCANNER POPUP ---
    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    BarcodeScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeDetected = { code ->
                            onValueChange(code)
                            showScanner = false
                        }
                    )
                    // Mira visual
                    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 4.dp.toPx()
                            val cornerLength = 30.dp.toPx()
                            val color = Color.White.copy(alpha = 0.8f)
                            // Top Left
                            drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                            drawLine(color, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)
                            // Top Right
                            drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
                            drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)
                            // Bottom Left
                            drawLine(color, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
                            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)
                            // Bottom Right
                            drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth)
                            drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth)
                        }
                    }
                    // Botão Fechar
                    IconButton(
                        onClick = { showScanner = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, "Fechar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}