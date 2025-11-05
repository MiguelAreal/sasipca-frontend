package g8.ipca.sasipca.sasipca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import g8.ipca.sasipca.sasipca.navigation.NavigationService
import g8.ipca.sasipca.sasipca.navigation.Screen
@Composable
fun Header(
    title: String,
    subTitle: String = ""
) {
    val currentScreen = NavigationService.currentScreen
    val showBackButton = Screen.isOverlay(currentScreen) && NavigationService.canGoBack()
    val showSettings = Screen.isSettings(currentScreen)

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
                        onClick = { NavigationService.goBack() },
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
                        onClick = { NavigationService.navigateTo(Screen.Settings) },
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
                    onClick = { NavigationService.navigateTo(Screen.Notifications) },
                    modifier = Modifier.size(40.dp)
                ) {
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
