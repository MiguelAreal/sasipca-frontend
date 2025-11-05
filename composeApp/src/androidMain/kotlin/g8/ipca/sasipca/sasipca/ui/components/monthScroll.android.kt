package g8.ipca.sasipca.sasipca.ui.components

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import java.time.YearMonth

// androidMain
@SuppressLint("ModifierFactoryUnreferencedReceiver")
actual fun Modifier.monthScroll(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit
): Modifier = pointerInput(month) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Scroll) {
                val scrollY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                if (scrollY > 24f) {
                    onMonthChange(month.minusMonths(1))
                } else if (scrollY < -24f) {
                    onMonthChange(month.plusMonths(1))
                }
            }
        }
    }
}
