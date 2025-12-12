package sasipca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sasipca.screens.navigation.LoginScreen
import sasipca.screens.navigation.MainScreen
import sasipca.screens.navigation.NotificationsScreen
import sasipca.screens.navigation.SettingsScreen
import sasipca.storage.NotificationManager

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun Header(
    title: String,
    subTitle: String = ""
) {

    val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

    val currentScreen = navigator.lastItem

    // Lógica para mostrar/esconder o botão de voltar
    val isRootScreen = currentScreen is MainScreen || currentScreen is LoginScreen

    val showBackButton = navigator.canPop && !isRootScreen

    // Esconde botão de settings se já estivermos nele
    val showSettings = currentScreen !is SettingsScreen

    val unreadCount by NotificationManager.unreadCount.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        val isCompact = maxWidth < 600.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 24.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (showBackButton) {
                    IconButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = if (isCompact) 18.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (subTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subTitle,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = if (isCompact) 12.sp else 13.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if(showSettings) {
                    IconButton(
                        // Agora este push vai para o Root Navigator, e não para as Tabs
                        onClick = { navigator.push(SettingsScreen()) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Definições",
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                    // Igual aqui para as notificações
                    onClick = { navigator.push(NotificationsScreen()) },
                    modifier = Modifier.size(40.dp)
                ) {
                    if (unreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = Color.White
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificações",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}