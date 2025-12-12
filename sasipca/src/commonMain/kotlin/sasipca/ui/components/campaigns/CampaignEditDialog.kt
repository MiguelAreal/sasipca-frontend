package sasipca.ui.components.campaigns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import sasipca.ui.components.ValidatedDateField
import sasipca.ui.components.ValidatedTextField
import sasipca.ui.theme.CardTitle
import sasipca.utils.rememberImagePickerLauncher
import sasipca.viewmodels.CampaignFormState
import sasipca.viewmodels.CampaignViewModel

@Composable
fun CampaignEditDialog(
    formState: CampaignFormState,
    onDismiss: () -> Unit,
    viewModel: CampaignViewModel
) {
    val imagePicker = rememberImagePickerLauncher { bytes ->
        viewModel.onImagePicked(bytes)
    }

    // Estado para o Dialog de Confirmação de Eliminar Campanha
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- 1. Header (Imagem) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // Lógica de visualização da imagem:
                    // Verifica se há imagem E se NÃO foi marcada para remover
                    val hasImage = (formState.newImageBytes != null || !formState.imageUrl.isNullOrEmpty())
                    val showImage = hasImage && !formState.removeImage

                    val modelToRender = if (formState.newImageBytes != null) {
                        formState.newImageBytes
                    } else formState.imageUrl

                    if (showImage && modelToRender != null) {
                        SubcomposeAsyncImage(
                            model = modelToRender,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        )
                    } else {
                        // Placeholder (Vazio ou Removido)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (formState.removeImage) {
                                Text(
                                    "Imagem removida",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // --- Overlay de Edição (Centro) ---
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.1f))
                            .clickable { imagePicker.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (formState.newImageBytes != null) "Nova Imagem" else "Alterar Imagem",
                                color = Color.White
                            )
                        }
                    }

                    // --- Botão Apagar Imagem (Canto Superior Esquerdo) ---
                    // Só mostra se houver imagem para apagar e não estiver já marcada
                    if (showImage) {
                        IconButton(
                            onClick = { viewModel.onRemoveImage() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remover Imagem",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // --- Botão Fechar Dialog (Canto Superior Direito) ---
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }

                    // --- Badge Dias (Canto Inferior Esquerdo) ---
                    CampaignStatusBadge(
                        startDateStr = formState.startDate,
                        endDateStr = formState.endDate,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }

                // --- 2. Formulário ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ... (Campos mantêm-se iguais) ...
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CardTitle("Informações Gerais")
                        ValidatedTextField(
                            value = formState.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = "Nome da Campanha",
                            error = formState.errors["name"],
                            modifier = Modifier.fillMaxWidth(),
                            maxLength = 80
                        )
                        ValidatedTextField(
                            value = formState.location,
                            onValueChange = { viewModel.onLocationChange(it) },
                            label = "Localização",
                            error = formState.errors["location"],
                            modifier = Modifier.fillMaxWidth(),
                            maxLength = 150
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CardTitle("Período")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ValidatedDateField(
                                value = convertDateToUi(formState.startDate),
                                onValueChange = { viewModel.onStartDateChange(it) },
                                label = "Início",
                                error = formState.errors["startDate"],
                                modifier = Modifier.weight(1f)
                            )
                            ValidatedDateField(
                                value = convertDateToUi(formState.endDate),
                                onValueChange = { viewModel.onEndDateChange(it) },
                                label = "Fim",
                                error = formState.errors["endDate"],
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CardTitle("Detalhes")
                        ValidatedTextField(
                            value = formState.description,
                            onValueChange = { viewModel.onDescriptionChange(it) },
                            label = "Descrição",
                            error = formState.errors["description"],
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            singleLine = false
                        )
                    }
                }

                // --- 3. Rodapé de Ações ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, // Espalha os itens
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Botão Eliminar Campanha (Esquerda) - Apenas se já existir (id != 0)
                    if (formState.id != 0) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Campanha")
                        }
                    } else {
                        Spacer(Modifier.width(1.dp)) // Spacer para manter layout se não houver botão
                    }

                    // Botões Cancelar e Guardar (Direita)
                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.saveCampaign() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    // --- Dialog de Confirmação de Eliminação ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Campanha") },
            text = { Text("Tem a certeza que deseja eliminar esta campanha? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCampaign()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


// Helper visual para UI Date
fun convertDateToUi(apiDate: String): String {
    if (apiDate.isBlank()) return ""
    return try {
        val parts = apiDate.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else apiDate
    } catch (e: Exception) { apiDate }
}
