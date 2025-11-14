package sasipca.ui.components.calendar

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen
import sasipca.storage.ScreenSizeManager.isSmallScreen
import sasipca.utils.convertMonthPt
import java.time.YearMonth

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun CalendarHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val small = isSmallScreen()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    vertical = 24.dp,
                    horizontal = if (small) 12.dp else 24.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔹 Left side: navigation controls
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (small) {
                    // Small-screen version: Icon only
                    IconButton(
                        onClick = onToday,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Hoje",
                            tint = Color.White
                        )
                    }
                } else {
                    // Large-screen version: Text button
                    Button(
                        onClick = onToday,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Text("Hoje", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(if (small) 4.dp else 8.dp))

                IconButton(
                    onClick = onPrev,
                    modifier = Modifier.size(if (small) 32.dp else 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "Mês anterior",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(if (small) 32.dp else 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "Próximo mês",
                        tint = Color.White
                    )
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

            // 🔹 Right side: actions (Settings, Notifications)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { NavigationService.navigateTo(Screen.Settings) },
                    modifier = Modifier.size(if (small) 32.dp else 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Definições",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { NavigationService.navigateTo(Screen.Notifications) },
                    modifier = Modifier.size(if (small) 32.dp else 40.dp)
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
