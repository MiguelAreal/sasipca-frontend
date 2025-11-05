package g8.ipca.sasipca.sasipca.ui.components

import androidx.compose.ui.Modifier
import java.time.YearMonth

expect fun Modifier.monthScroll(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit
): Modifier