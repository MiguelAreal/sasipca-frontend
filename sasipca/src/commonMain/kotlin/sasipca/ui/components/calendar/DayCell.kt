package sasipca.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.Delivery
import sasipca.storage.ScreenSizeManager.isSmallScreen
import java.time.LocalDate

@Composable
fun DayCell(
    day: LocalDate,
    deliveriesForDay: List<Delivery>,
    isToday: Boolean,
    isRealCurrentMonth: Boolean,
    onEventClick: (Delivery) -> Unit
) {
    val isSmall = isSmallScreen()

    // Cores de Texto (Garante contraste no Light Mode)
    val dayNumberColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimary
    } else if (isRealCurrentMonth) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    val dayNumberBg = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Centra conteúdo no mobile
    ) {
        // --- NÚMERO DO DIA ---
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(dayNumberBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                fontWeight = if(isToday) FontWeight.Bold else FontWeight.Normal,
                color = dayNumberColor,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- INDICADORES DE ENTREGAS ---
        if (deliveriesForDay.isNotEmpty()) {
            if (isSmall) {
                // MOBILE: Apenas bolinha com número
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Text(
                        text = deliveriesForDay.size.toString(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            } else {
                // ‘DESKTOP’: Chips/Barras com nome
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    deliveriesForDay.take(3).forEach { delivery ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                // Fundo com pouca opacidade para não ficar pesado no Light Mode
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable { onEventClick(delivery) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = delivery.beneficiaryName ?: "Entrega",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (deliveriesForDay.size > 3) {
                        Text(
                            "+ ${deliveriesForDay.size - 3}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}