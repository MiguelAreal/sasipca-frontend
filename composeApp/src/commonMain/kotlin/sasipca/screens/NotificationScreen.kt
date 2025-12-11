package sasipca.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sasipca.storage.NotificationManager
import sasipca.models.Notification
import sasipca.repositories.NotificationRepository
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget

@Composable
fun NotificationsScreen(notificationRepository: NotificationRepository) {
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val loadNotifications = {
        scope.launch {
            notifications = notificationRepository.getNotifications()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
        // Marcar badge como limpo ao entrar no ecrã (se quiseres essa lógica)
        NotificationManager.refreshCount()
    }

    LaunchedEffect(Unit) {
        NotificationManager.reloadTrigger.collect {
            loadNotifications()
        }
    }

    // Ao clicar no cartão, marca como lido
    fun handleMarkAsRead(notification: Notification) {
        if (!notification.isRead) {
            notifications = notifications.map {
                if (it.id == notification.id) it.copy(isRead = true) else it
            }
            NotificationManager.decrementCount()

            scope.launch {
                notificationRepository.markAsRead(notification.id)
            }
        }
    }

    fun handleArchive(notificationId: Int) {
        val item = notifications.find { it.id == notificationId }
        notifications = notifications.filter { it.id != notificationId }

        if (item != null && !item.isRead) {
            NotificationManager.decrementCount()
        }

        scope.launch {
            notificationRepository.archive(notificationId)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(title = "Notificações")

        if (isLoading) {
            LoadingWidget()
        } else if (notifications.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp) // Linha fina entre itens se não usarmos Card Elevation
            ) {
                items(notifications, key = { it.id }) { notif ->
                    SwipeToArchiveItem(
                        item = notif,
                        onArchive = { handleArchive(notif.id) },
                        content = {
                            NotificationItemRow(
                                notification = notif,
                                onClick = { handleMarkAsRead(notif) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToArchiveItem(
    item: Notification,
    onArchive: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onArchive()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.background
            )
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Eliminar",
                    modifier = Modifier.scale(scale),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = { content() }
    )
}

@Composable
fun NotificationItemRow(
    notification: Notification,
    onClick: () -> Unit
) {
    // Formatar data simples (ex: "10:30" ou "09/12")
    // Podes melhorar isto com lógica de "Hoje", "Ontem", etc.
    val dateDisplay = notification.date.replace("T", " ").take(16).substring(5)

    // Configuração de estilo baseado no estado
    val backgroundColor = if (!notification.isRead)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) // Leve tom azulado
    else
        MaterialTheme.colorScheme.surface

    val messageFontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal
    val messageColor = if (!notification.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top // Alinhar ao topo caso o texto seja longo
        ) {
            // 1. INDICADOR (Bolinha ou Ícone)
            Column(
                modifier = Modifier.padding(top = 4.dp), // Ajuste fino para alinhar com a primeira linha de texto
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!notification.isRead) {
                    // Bolinha de Não Lido
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    // Espaço vazio para manter alinhamento ou ícone cinza
                    // Podes usar um ícone genérico ou a foto do remetente se houver
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(16.dp) // Mais pequeno que o normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. CONTEÚDO
            Column(modifier = Modifier.weight(1f)) {
                // Linha superior: Título (se houver) ou início da mensagem + Data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = messageFontWeight,
                    color = messageColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
        }
    }
    // Separador (opcional, estilo lista iOS/Android nativo)
    HorizontalDivider(
        modifier = Modifier.padding(start = 42.dp), // Começa depois do ícone
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tudo limpo!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Não tem notificações novas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}