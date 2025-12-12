package sasipca.ui.components.campaigns

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun CampaignStatusBadge(
    startDateStr: String,
    endDateStr: String,
    modifier: Modifier = Modifier
) {
    // Cores personalizadas
    val GreenBack = Color(0xFFE6F4EA)
    val GreenText = Color(0xFF1E8E3E)
    val RedBack = Color(0xFFFCE8E6)
    val RedText = Color(0xFFC5221F)
    val BlueBack = Color(0xFFE3F2FD) // Azul para "Brevemente"
    val BlueText = Color(0xFF1565C0)
    val OrangeBack = Color(0xFFFFF3E0)
    val OrangeText = Color(0xFFE65100)

    val badgeState = remember(startDateStr, endDateStr) {
        try {
            val start = LocalDate.parse(startDateStr)
            val end = LocalDate.parse(endDateStr)
            val now = LocalDate.now()

            when {
                // 1. Ainda não começou (Futuro)
                now.isBefore(start) -> {
                    val daysToStart = ChronoUnit.DAYS.between(now, start)
                    val text = if (daysToStart == 1L) "Inicia amanhã" else "Inicia em $daysToStart dias"
                    Triple(BlueBack, BlueText, text)
                }
                // 2. Já acabou (Passado)
                now.isAfter(end) -> {
                    Triple(RedBack, RedText, "Terminada")
                }
                // 3. A decorrer (Presente)
                else -> {
                    val daysToEnd = ChronoUnit.DAYS.between(now, end)
                    when {
                        daysToEnd == 0L -> Triple(OrangeBack, OrangeText, "Termina hoje")
                        daysToEnd <= 3 -> Triple(RedBack, RedText, "Termina em $daysToEnd dias") // Urgente
                        else -> Triple(GreenBack, GreenText, "$daysToEnd dias restantes") // Normal
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback caso as datas estejam inválidas ou vazias durante a edição
            Triple(Color.LightGray, Color.DarkGray, "--")
        }
    }

    val (bgColor, txtColor, text) = badgeState

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = txtColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = txtColor
            )
        }
    }
}