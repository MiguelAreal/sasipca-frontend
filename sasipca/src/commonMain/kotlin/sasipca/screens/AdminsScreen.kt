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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sasipca.models.AdminUser
import sasipca.models.SnackbarType
import sasipca.repositories.AdminRepository
import sasipca.ui.components.Header
import sasipca.ui.components.LinearLoadingWidget
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.SearchInputField
import sasipca.ui.components.ValidatedTextField
import sasipca.utils.SnackbarManager
import sasipca.viewmodels.AdminViewModel

@Composable
fun AdminsScreen(
    adminRepository: AdminRepository
) {
    val viewModel = remember { AdminViewModel(adminRepository) }

    // Estados do ViewModel (StateFlows convertidos para State)
    val uiState by viewModel.uiState.collectAsState()

    // Estados Locais de UI
    var showAddDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var newContact by remember { mutableStateOf("") }
    var searchTerm by remember { mutableStateOf("") }

    // 1. Carregamento Inicial
    LaunchedEffect(Unit) {
        viewModel.loadAdmins()
    }

    // 2. Lógica de Pesquisa com Debounce
    LaunchedEffect(searchTerm) {
        // Se não estiver vazio, espera para não fazer pedidos a cada letra
        if (searchTerm.isNotEmpty()) {
            delay(500)
        }
        // Carrega (seja pesquisa ou reset para todos se vazio)
        viewModel.loadAdmins(query = searchTerm, page = 1)
    }

    // 3. Gestão de ‘Feedback’ (Sucesso na criação)
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            SnackbarManager.show("Administrador criado com sucesso!", SnackbarType.SUCCESS)
            showAddDialog = false
            newEmail = ""
            newContact = ""
            viewModel.clearUiState()
            // Recarregar a lista para mostrar o novo admin
            viewModel.loadAdmins(query = searchTerm)
        }
    }

    // 4. Gestão de Erros (Criação ou Listagem)
    LaunchedEffect(uiState.lastErrorMessage) {
        uiState.lastErrorMessage?.let { msg ->
            SnackbarManager.show(msg, SnackbarType.ERROR)
            viewModel.clearFeedbackMessages()
        }
    }

    // Erros específicos de carregamento da lista (se existirem separados no VM)
    LaunchedEffect(viewModel.errorMessageList) {
        viewModel.errorMessageList?.let {
            SnackbarManager.show(it, SnackbarType.ERROR)
        }
    }

    // Estrutura Principal
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {

            Header("Administradores")

            // Loading Linear abaixo do Header (apenas se a lista estiver a carregar)
            if (viewModel.isLoadingList) {
                LinearLoadingWidget()
            }

            // --- BARRA DE PESQUISA ---
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
                    // Loading inicial (ecrã limpo)
                    LoadingWidget()
                } else if (viewModel.admins.isEmpty() && !viewModel.isLoadingList) {
                    // Estado Vazio
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Nenhum administrador encontrado.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // Lista com Dados + Paginação
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Lista
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
                        // Só mostra se houver mais que uma página
                        if (viewModel.totalPages > 1) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 16.dp // Sombra mais forte para destacar do fundo
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 20.dp),
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
            }
        }

        // --- FAB (FLOATING ACTION BUTTON) ---
        FloatingActionButton(
            onClick = {
                newEmail = "";
                newContact = "";
                viewModel.clearUiState();
                showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                // Adiciona padding extra se a paginação estiver visível para não tapar
                .padding(bottom = if (viewModel.totalPages > 1) 48.dp else 0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Admin", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    // --- DIALOG PARA ADICIONAR ADMIN ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {showAddDialog = false},
            title = { Text("Novo Administrador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Adicione o email institucional e contacto do novo administrador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Campo de Endereço eletrónico
                    ValidatedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = "Email institucional",
                        error = uiState.errors["email"],
                        singleLine = true,
                        keyboardType = KeyboardType.Email
                    )

                    // Campo de Contacto
                    ValidatedTextField(
                        value = newContact,
                        onValueChange = { newContact = it },
                        label = "Contacto",
                        error = uiState.errors["contact"],
                        singleLine = true,
                        maxLength = 15,
                        keyboardType = KeyboardType.Phone
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createAdmin(newEmail, newContact)
                    },
                    enabled = !uiState.isLoading && newEmail.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Criar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false},
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AdminCard(admin: AdminUser) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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