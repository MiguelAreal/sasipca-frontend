package sasipca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.* // Use wildcards to ensure all runtime triggers are caught
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sasipca.navigation.LoginScreen
import sasipca.navigation.MainScreen
import sasipca.navigation.NotificationsScreen
import sasipca.navigation.SettingsScreen
import sasipca.storage.NotificationManager
import sasipca.storage.SessionManager.isLoggedIn

@Composable
fun Header(
    title: String,
    subTitle: String = ""
) {
    val navigator = LocalNavigator.currentOrThrow.let { nav ->
        var root = nav
        while (root.parent != null) root = root.parent!!
        root
    }

    val currentScreen = navigator.lastItem
    val isRootScreen = currentScreen is MainScreen || currentScreen is LoginScreen
    val showBackButton = navigator.canPop && !isRootScreen
    val showSettings = currentScreen !is SettingsScreen && currentScreen !is NotificationsScreen
    val showNotifications = isLoggedIn.collectAsState().value && currentScreen !is NotificationsScreen && currentScreen !is SettingsScreen

    // 2. State Collection
    val unreadCount by NotificationManager.unreadCount.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        // Use the scope-provided 'maxWidth' safely
        val isCompact = maxWidth < 600.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 24.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderTitleSection(
                showBackButton = showBackButton,
                title = title,
                subTitle = subTitle,
                isCompact = isCompact,
                onBack = { navigator.pop() }
            )

            HeaderActionSection(
                showSettings = showSettings,
                showNotifications = showNotifications,
                unreadCount = unreadCount,
                onSettingsClick = { navigator.push(SettingsScreen()) },
                onNotificationsClick = { navigator.push(NotificationsScreen()) }
            )
        }
    }
}

@Composable
private fun HeaderTitleSection(
    showBackButton: Boolean,
    title: String,
    subTitle: String,
    isCompact: Boolean,
    onBack: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
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
}

@Composable
private fun HeaderActionSection(
    showSettings: Boolean,
    showNotifications: Boolean,
    unreadCount: Int,
    onSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showSettings) {
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Definições",
                    tint = Color.White
                )
            }
        }

        if (showNotifications) {
            IconButton(onClick = onNotificationsClick, modifier = Modifier.size(40.dp)) {
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