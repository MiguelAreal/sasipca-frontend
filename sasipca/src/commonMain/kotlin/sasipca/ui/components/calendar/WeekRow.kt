package sasipca.ui.components.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import sasipca.models.Delivery
import java.time.LocalDate

@Composable
fun WeekRow(
    startOfWeek: LocalDate,
    deliveries: List<Delivery>,
    onDayClick: (LocalDate, List<Delivery>) -> Unit,
    onEventClick: (Delivery) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val today = LocalDate.now()

    Row(modifier = modifier) {
        days.forEach { day ->
            key(day.toEpochDay()) {
                val deliveriesForDay by rememberUpdatedState(
                    deliveries.filter {
                        val scheduled = LocalDate.parse(it.scheduledDate)
                        scheduled.year == day.year && scheduled.dayOfYear == day.dayOfYear
                    }
                )

                val isToday = day.year == today.year && day.dayOfYear == today.dayOfYear

                // Lógica corrigida: A cor baseia-se no Mês REAL do sistema, não no ‘scroll’
                val isRealCurrentMonth = day.month == today.month && day.year == today.year

                // Cores ajustadas para Tema Light e Dark
                val containerColor = if (isRealCurrentMonth) {
                    MaterialTheme.colorScheme.surface // Branco no Light, Cinza escuro no Dark
                } else {
                    // Mês passado ou futuro real ⇾ Cor de fundo ligeiramente diferente
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                }

                Card(
                    onClick = { onDayClick(day, deliveriesForDay) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RectangleShape, // Grelha perfeita
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    // Borda subtil para separar dias
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    DayCell(
                        day = day,
                        deliveriesForDay = deliveriesForDay,
                        isToday = isToday,
                        isRealCurrentMonth = isRealCurrentMonth, // Passamos esta flag para controlar opacidade do texto
                        onEventClick = onEventClick
                    )
                }
            }
        }
    }
}