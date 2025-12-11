package sasipca.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    // Lógica de abertura do dropdown
    LaunchedEffect(value, suggestions) {
        // Só abre se estivermos a escrever manualmente (não pelo scanner)
        // e se houver texto
        expanded = value.isNotEmpty() && suggestions.isNotEmpty() && !showScanner
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
                trailingIcon = {
                    Row {
                        // Botão Scanner
                        IconButton(onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ->
                                    showScanner = true
                                else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(Icons.Default.Scanner, contentDescription = "Ler código")
                        }
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

    // --- Overlay do Scanner (Copiado da tua implementação anterior) ---
    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                BarcodeScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeDetected = { code ->
                        // Ao detetar, preenche o valor e fecha scanner
                        onValueChange(code)
                        showScanner = false
                        expanded = false // Fecha dropdown se abrir
                    }
                )

                // Guia visual (Moldura)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f) // Quadrado no telemóvel
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 4.dp.toPx()
                        val cornerLength = 40.dp.toPx()
                        // (Desenho das linhas brancas igual ao teu código anterior)
                        // Cantos Topo Esquerdo
                        drawLine(Color.White, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                        drawLine(Color.White, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)
                        // Topo Direito
                        drawLine(Color.White, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
                        drawLine(Color.White, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)
                        // Baixo Esquerdo
                        drawLine(Color.White, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
                        drawLine(Color.White, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)
                        // Baixo Direito
                        drawLine(Color.White, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth)
                        drawLine(Color.White, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth)
                    }
                }

                IconButton(
                    onClick = { showScanner = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Text("X", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}