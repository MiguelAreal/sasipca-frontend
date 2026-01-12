package sasipca.screens

import sasipca.AppConfig
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sasipca.network.ApiClient
import sasipca.storage.SettingsManager
import sasipca.ui.components.Header
import sasipca.models.SnackbarType
import sasipca.utils.SnackbarManager
import kotlinx.coroutines.launch
import sasipca.navigation.LoginScreen
import sasipca.storage.SessionManager

@Composable
fun SettingsScreen() {
    // 1. Obter o Navigator Principal (para sair do Login se necessário)
    val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

    var serverIp by remember { mutableStateOf(SettingsManager.getServerIp()) }
    val isDarkTheme by SettingsManager.isDarkThemeFlow.collectAsState()
    var showIpDialog by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf(serverIp) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val authRepository = ApiClient.authRepository

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Header("Definições", "Configure a aplicação")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- APARÊNCIA ---
            SectionHeader("Aparência")
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Modo Escuro",
                    description = "Alterar tema da aplicação",
                    checked = isDarkTheme,
                    onCheckedChange = { enabled ->
                        SettingsManager.setDarkTheme(enabled)
                    }
                )
            }

            // --- REDE ---
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

            // --- CONTA (LOGOUT) ---
            if (SessionManager.isLoggedInNow()) {
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
                                } catch (_: Exception) {
                                    // Ignora erro de rede no logout, limpa sessão local na mesma
                                }
                                // Redireciona para o Login e limpa a pilha
                                navigator.replaceAll(LoginScreen())
                            }
                        }
                    )
                }
            }

            // --- SOBRE ---
            SectionHeader("Sobre")
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "Projeto SASIPCA",
                    description = "Visite o nosso website para mais informações",
                    onClick = {
                        uriHandler.openUri("https://sasipca.rapi4real.duckdns.org")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingsTextItem(
                    icon = Icons.Default.Groups,
                    title = "Grupo 8",
                    description = "João Lopes | Júlio Faria | Paulo Costa | Miguel Areal"
                )
            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Versão ${AppConfig.VERSION}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "SASIPCA © 2026",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }


    // --- DIALOG SERVER IP ---
    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = {showIpDialog = false },
            title = { Text("Configurar Servidor", fontWeight = FontWeight.Bold) },
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
                            SnackbarManager.show("Servidor atualizado: $tempIp", SnackbarType.SUCCESS)
                        } else {
                            SnackbarManager.show("Endereço não pode estar vazio", SnackbarType.ERROR)
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
}
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