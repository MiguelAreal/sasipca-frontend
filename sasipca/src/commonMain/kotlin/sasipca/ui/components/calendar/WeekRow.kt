package sasipca.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sasipca.models.Delivery
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun WeekRow(
    startOfWeek: LocalDate,
    focusedMonth: YearMonth,
    deliveries: List<Delivery>,
    onDayClick: (LocalDate, List<Delivery>) -> Unit,
    onEventClick: (Delivery) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val today = LocalDate.now()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            key("week_${startOfWeek.toEpochDay()}_day_${day.toEpochDay()}") {

                val deliveriesForDay by rememberUpdatedState(
                    deliveries.filter { LocalDate.parse(it.scheduledDate) == day }
                )

                val isToday = day == today
                val isFuture = day > today
                val isPast = day < today
                val isCurrentMonth = day.month == focusedMonth.month

                // Texto: cor normal, dia de outro mês mais claro
                val textColor = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onSurface
                }

                // Container: fundo mais escuro para dias fora do mês ou passado
                val containerColor = when {
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    !isCurrentMonth || isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    onClick = { onDayClick(day, deliveriesForDay) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isToday -> MaterialTheme.colorScheme.secondaryContainer
                            !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = null
                ) {
                    DayCell(
                        day = day,
                        deliveriesForDay = deliveriesForDay,
                        textColor = textColor,
                        onEventClick = onEventClick
                    )
                }
            }
        }
    }
}