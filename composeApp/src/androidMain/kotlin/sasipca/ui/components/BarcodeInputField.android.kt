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
import androidx.compose.material.icons.filled.QrCodeScanner // Ícone mais moderno
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

    // Permissões de câmara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (granted) showScanner = true }
    )

    // Lógica para abrir/fechar sugestões ao escrever
    LaunchedEffect(value, suggestions) {
        if (showScanner) {
            expanded = false
        } else {
            expanded = value.isNotEmpty() && suggestions.isNotEmpty()
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
                    .menuAnchor(), // Essencial para ligar ao menu
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    IconButton(onClick = {
                        // Verifica permissão antes de abrir
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

            // Menu de Sugestões
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

    // --- POPUP DO SCANNER ---
    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false }
            // Sem properties especiais, o Dialog fecha ao clicar fora por defeito
        ) {
            // Container do Scanner (Cartão Flutuante)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f), // Formato retangular alto, bom para scanner
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    // 1. Preview da Câmara
                    BarcodeScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeDetected = { code ->
                            onValueChange(code)
                            showScanner = false
                        }
                    )

                    // 2. Guia Visual (Mira)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp), // Margem da mira
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 4.dp.toPx()
                            val cornerLength = 30.dp.toPx()
                            val color = Color.White.copy(alpha = 0.8f)

                            // Desenho dos cantos da mira
                            // Topo Esquerdo
                            drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                            drawLine(color, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)
                            // Topo Direito
                            drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
                            drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)
                            // Baixo Esquerdo
                            drawLine(color, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
                            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)
                            // Baixo Direito
                            drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth)
                            drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth)
                        }
                    }

                    // 3. Botão de Fechar Estilizado
                    IconButton(
                        onClick = { showScanner = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)) // Fundo escuro semitransparente
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Scanner",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                }
            }
        }
    }
}