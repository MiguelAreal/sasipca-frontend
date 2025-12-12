package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import sasipca.repositories.AdminRepository
import sasipca.models.AdminUser
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.ValidatedTextField
import sasipca.utils.SnackbarManager
import sasipca.models.SnackbarType
import sasipca.ui.components.SearchInputField
import sasipca.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminsScreen(
    adminRepository: AdminRepository
) {
    val viewModel = remember { AdminViewModel(adminRepository) }

    // Estados do ViewModel
    val uiState by viewModel.uiState.collectAsState() // Observa o estado de feedback

    // Estados Locais
    var showAddDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var newContact by remember { mutableStateOf("") }
    var searchTerm by remember { mutableStateOf("") }

    // Carregar dados inicial
    LaunchedEffect(Unit) {
        viewModel.loadAdmins()
    }

    // Debounce de pesquisa (Tal como no BeneficiariesScreen)
    LaunchedEffect(searchTerm) {
        // Se o utilizador estiver a escrever, esperamos 500ms (Debounce).
        if (searchTerm.isNotEmpty()) {
            delay(500)
        }
        viewModel.loadAdmins(query = searchTerm, page = 1)
    }

    // Feedback de Sucesso/Erro
    LaunchedEffect(searchTerm) {
        if (searchTerm.isEmpty()) {
            viewModel.loadAdmins()
        } else {
            delay(500)
            viewModel.loadAdmins(query = searchTerm, page = 1)
        }
    }

    // Feedback de Sucesso/Erro
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            SnackbarManager.show("Administrador criado com sucesso!", SnackbarType.SUCCESS)
            showAddDialog = false
            newEmail = ""
            newContact = ""
            viewModel.clearUiState()
        }
    }
    LaunchedEffect(uiState.lastErrorMessage) {
        uiState.lastErrorMessage?.let { msg ->
            SnackbarManager.show(msg, SnackbarType.ERROR)
            viewModel.clearUiState()
        }
    }
    LaunchedEffect(viewModel.errorMessageList) {
        viewModel.errorMessageList?.let {
            SnackbarManager.show(it, SnackbarType.ERROR)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {

            Header("Administradores")

            // --- BARRA DE PESQUISA (Estilo BeneficiariesScreen) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchInputField(
                    query = searchTerm,
                    onQueryChange = { searchTerm = it },
                    placeholder = "Nome, Email ou Contacto...",
                    modifier = Modifier.weight(1f)
                )
            }

            // --- CONTEÚDO DA LISTA ---
            Box(modifier = Modifier.weight(1f)) {

                if (viewModel.isLoadingList && viewModel.admins.isEmpty()) {
                    // Loading inicial sem dados
                    LoadingWidget()
                } else if (viewModel.admins.isEmpty() && !viewModel.isLoadingList) {
                    // Estado Vazio
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum administrador encontrado.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                } else {
                    // Lista com Dados
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.admins, key = { it.id }) { admin ->
                                AdminCard(admin)
                            }
                        }

                        // --- RODAPÉ DE PAGINAÇÃO ---
                        if (viewModel.totalPages > 1) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.goToPreviousPage() },
                                        enabled = viewModel.currentPage > 1 && !viewModel.isLoadingList
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior")
                                    }

                                    Text(
                                        text = "Página ${viewModel.currentPage} de ${viewModel.totalPages}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = { viewModel.goToNextPage() },
                                        enabled = viewModel.currentPage < viewModel.totalPages && !viewModel.isLoadingList
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Seguinte")
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading Overlay (barra de progresso no topo se estiver a paginar/filtrar mas já tem dados)
                if (viewModel.isLoadingList && viewModel.admins.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }
            }
        }

        // --- FAB (FLOATING ACTION BUTTON) ---
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Admin", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    // --- DIALOG PARA ADICIONAR ADMIN ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isLoading) showAddDialog = false
            },
            title = { Text("Novo Administrador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Adicione o email institucional e contacto do novo administrador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Usa o componente validado que pediste
                    ValidatedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = "Email institucional",
                        error = uiState.errors["email"],
                        singleLine = true,
                        keyboardType = KeyboardType.Email
                    )

                    ValidatedTextField(
                        value = newContact,
                        onValueChange = { newContact = it },
                        label = "Contacto",
                        error = uiState.errors["contact"],
                        singleLine = true,
                        keyboardType = KeyboardType.Phone
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createAdmin(newEmail, newContact)
                    },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Criar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- CARD DE ADMIN (Mantém-se igual, apenas com ajustes visuais se necessário) ---
@Composable
fun AdminCard(admin: AdminUser) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Ajustado para 1dp para ser mais leve
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Iniciais)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if(admin.name.isNotEmpty()) admin.name.take(1).uppercase() else "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = admin.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = admin.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = admin.contact,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}