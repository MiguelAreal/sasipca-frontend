package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen
import sasipca.ApiClient
import sasipca.repositories.AuthRepository
import sasipca.storage.SettingsManager
import sasipca.ui.components.Header
import sasipca.utils.SnackbarType
import sasipca.utils.SnackbarManager
import kotlinx.coroutines.launch
import sasipca.storage.SessionManager

@Composable
fun SettingsScreen(onThemeChanged: (Boolean) -> Unit) {
    var serverIp by remember { mutableStateOf(SettingsManager.getServerIp()) }
    var isDarkTheme by remember { mutableStateOf(SettingsManager.isDarkTheme()) }
    var showIpDialog by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf(serverIp) }
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(ApiClient.client) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Header("Definições", "Configure a aplicação")

        BoxWithConstraints {
            val horizontalPadding = if (maxWidth < 600.dp) 20.dp else 40.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /**
                 * Secção de alteração de tema.
                 */
                SectionHeader("Aparência")
                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        title = "Modo Escuro",
                        description = "Alterar tema da aplicação",
                        checked = isDarkTheme,
                        onCheckedChange = { enabled ->
                            isDarkTheme = enabled
                            SettingsManager.setDarkTheme(enabled)
                            onThemeChanged(enabled)
                            SnackbarManager.show(
                                message = "Tema alterado com sucesso",
                                type = SnackbarType.SUCCESS
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Secção de definições de rede.
                 */
                SectionHeader("Rede")
                SettingsCard {
                    SettingsClickableItem(
                        icon = Icons.Default.Storage,
                        title = "Servidor",
                        description = serverIp,
                        onClick = {
                            tempIp = serverIp
                            showIpDialog = true
                        },
                    )
                }

                /**
                 * Secção de Logout.
                 * Apenas mostra secção se o utilizador estiver com sessão iniciada.
                 */
                if (SessionManager.isLoggedInNow()){
                    SectionHeader("Conta")
                    SettingsCard {
                        SettingsClickableItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = "Terminar Sessão",
                            description = "Sair da aplicação",
                            onClick = {
                                scope.launch {
                                    try {
                                        authRepository.logout()
                                    } catch (e: Exception) {
                                    }

                                    NavigationService.resetTo(Screen.Login)
                                }
                            }
                        )
                    }
                }


                /**
                 * Secção de Informações do Projeto
                 */
                SectionHeader("Sobre")
                SettingsCard {
                    SettingsTextItem(
                        icon = Icons.Default.Info,
                        title = "Projeto SASIPCA",
                        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                    )
                }

            }
        }
    }

    // Server IP Dialog
    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = {
                Text(
                    text = "Configurar Servidor",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Insira o endereço do servidor",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        label = { Text("Endereço do Servidor") },
                        placeholder = { Text("Ex: 192.168.1.100") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempIp.isNotBlank()) {
                            serverIp = tempIp
                            SettingsManager.setServerIp(tempIp)
                            showIpDialog = false
                            SnackbarManager.show(
                                message = "Servidor atualizado: $tempIp",
                                type = SnackbarType.SUCCESS
                            )
                        } else {
                            SnackbarManager.show(
                                message = "Endereço não pode estar vazio",
                                type = SnackbarType.ERROR
                            )
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Header de Secção
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

/**
 * Card para apresentar algum componente
 */
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            content()
        }
    }
}

/**
 * Item de toggle ON/OFF para opções
 */
@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Item de texto
 */
@Composable
fun SettingsTextItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

