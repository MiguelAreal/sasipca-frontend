package sasipca.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.Delivery
import java.time.LocalDate


@Composable
fun DayCell(
    day: LocalDate,
    deliveriesForDay: List<Delivery>,
    textColor: Color,
    onEventClick: (Delivery) -> Unit,
    compactMode: Boolean = false
) {
    val today = LocalDate.now()
    val isToday = day == today

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        // Sempre mostrar o número do dia
        Text(
            text = day.dayOfMonth.toString(),
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        // Se for hoje e não houver entregas, não mostrar nada
        if (!isToday || deliveriesForDay.isNotEmpty()) {
            if (deliveriesForDay.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                if (compactMode) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 2.dp)
                            .background(Color(0xFF2ECC71), shape = RoundedCornerShape(50))
                    )
                } else {
                    deliveriesForDay.take(2).forEach { delivery ->
                        SuggestionChip(
                            onClick = { onEventClick(delivery) },
                            label = {
                                Text(
                                    text = delivery.beneficiaryName ?: "Entrega",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.height(18.dp),
                            border = null
                        )
                    }
                }
            }
        }
    }
}