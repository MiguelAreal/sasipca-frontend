package sasipca.ui.components.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sasipca.navigation.NotificationsScreen
import sasipca.navigation.SettingsScreen
import sasipca.storage.ScreenSizeManager.isSmallScreen
import sasipca.utils.convertMonthPt
import java.time.YearMonth
@Composable
fun CalendarHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow.let { nav ->
        var root = nav
        while (root.parent != null) root = root.parent!!
        root
    }

    // Use your existing manager instead of BoxWithConstraints
    val small = isSmallScreen()

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // We removed BoxWithConstraints here to avoid the scope mismatch
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            CalendarHeaderContent(
                small = small,
                month = month,
                onToday = onToday,
                onPrev = onPrev,
                onNext = onNext,
                onSettings = { navigator.push(SettingsScreen()) },
                onNotifications = { navigator.push(NotificationsScreen()) }
            )
        }
    }
}

@Composable
private fun CalendarHeaderContent(
    small: Boolean,
    month: YearMonth,
    onToday: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSettings: () -> Unit,
    onNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 24.dp,
                horizontal = if (small) 12.dp else 24.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left side: navigation controls ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (small) {
                IconButton(onClick = onToday, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Today, "Hoje", tint = Color.White)
                }
            } else {
                Button(
                    onClick = onToday,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Text("Hoje", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(if (small) 4.dp else 8.dp))

            IconButton(onClick = onPrev, modifier = Modifier.size(if (small) 32.dp else 40.dp)) {
                Icon(Icons.Filled.ArrowUpward, "Mês anterior", tint = Color.White)
            }

            IconButton(onClick = onNext, modifier = Modifier.size(if (small) 32.dp else 40.dp)) {
                Icon(Icons.Filled.ArrowDownward, "Próximo mês", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(if (small) 6.dp else 12.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = convertMonthPt(month.monthValue),
                    color = Color.White,
                    fontSize = if (small) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = month.year.toString(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = if (small) 11.sp else 13.sp
                )
            }
        }

        // --- Right side: actions ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSettings, modifier = Modifier.size(if (small) 32.dp else 40.dp)) {
                Icon(Icons.Default.Settings, "Definições", tint = Color.White)
            }

            IconButton(onClick = onNotifications, modifier = Modifier.size(if (small) 32.dp else 40.dp)) {
                Icon(Icons.Default.Notifications, "Notificações", tint = Color.White)
            }
        }
    }
}