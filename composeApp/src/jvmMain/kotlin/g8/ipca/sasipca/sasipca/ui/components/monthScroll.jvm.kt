package g8.ipca.sasipca.sasipca.ui.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import java.time.YearMonth
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.monthScroll(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit
): Modifier {
    var scrollAccumulator = 0f

    return onPointerEvent(PointerEventType.Scroll) { event ->
        val scrollY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
        scrollAccumulator += scrollY

        // threshold controla a sensibilidade do scroll
        val threshold = 3f

        if (abs(scrollAccumulator) >= threshold) {
            if (scrollAccumulator > 0) {
                // Scroll para cima → mês anterior
                onMonthChange(month.minusMonths(1))
            } else {
                // Scroll para baixo → mês seguinte
                onMonthChange(month.plusMonths(1))
            }
            // Reduz acumulador gradualmente para permitir scroll contínuo
            scrollAccumulator = 0f
        }
    }
}
