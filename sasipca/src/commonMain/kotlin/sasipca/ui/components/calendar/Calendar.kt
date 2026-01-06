package sasipca.ui.components.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sasipca.models.Delivery
import sasipca.utils.monthScroll
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class WeekCalendarController(
    val scrollToNextMonth: () -> Unit,
    val scrollToPreviousMonth: () -> Unit,
    val scrollToToday: () -> Unit
)
@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    month: YearMonth,
    startDate: LocalDate = LocalDate.now(),
    deliveries: List<Delivery> = emptyList(),
    onMonthChange: (YearMonth) -> Unit,
    onDayClick: (LocalDate, List<Delivery>) -> Unit,
    onEventClick: (Delivery) -> Unit,
    controller: (WeekCalendarController) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val today = LocalDate.now()

    val weeks = remember(startDate) { generateWeeksAround(startDate, 200) }

    val todayWeekIndex = remember(weeks) {
        weeks.indexOfFirst { weekStart ->
            val weekEnd = weekStart.plusDays(6)
            today in weekStart..weekEnd
        }.coerceAtLeast(0)
    }

    // Handlers and Controllers (Keep as you had them)
    LaunchedEffect(Unit) { listState.scrollToItem(todayWeekIndex) }
    LaunchedEffect(Unit) {
        controller(WeekCalendarController(
            scrollToNextMonth = {
                coroutineScope.launch {
                    listState.animateScrollToItem((listState.firstVisibleItemIndex + 4).coerceAtMost(weeks.lastIndex))
                }
            },
            scrollToPreviousMonth = {
                coroutineScope.launch {
                    listState.animateScrollToItem((listState.firstVisibleItemIndex - 4).coerceAtLeast(0))
                }
            },
            scrollToToday = { coroutineScope.launch { listState.animateScrollToItem(todayWeekIndex) } }
        ))
    }

    Column(modifier = modifier.fillMaxSize()) {
        WeekHeader(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))

        Box(modifier = Modifier.weight(1f)) {
            CalendarList(
                listState = listState,
                weeks = weeks,
                month = month,
                deliveries = deliveries,
                onDayClick = onDayClick,
                onEventClick = onEventClick,
                coroutineScope = coroutineScope
            )
        }
    }

    // Scroll Detection Logic
    val firstVisibleWeekIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleWeekIndex) {
        val midIndex = (firstVisibleWeekIndex + 2).coerceIn(weeks.indices)
        val midWeekDate = weeks[midIndex].plusDays(3)
        val newMonth = YearMonth.from(midWeekDate)

        // FIXED: Value comparison instead of identity comparison
        if (newMonth.monthValue != month.monthValue || newMonth.year != month.year) {
            onMonthChange(newMonth)
        }
    }
}

@Composable
private fun CalendarList(
    listState: LazyListState,
    weeks: List<LocalDate>,
    month: YearMonth,
    deliveries: List<Delivery>,
    onDayClick: (LocalDate, List<Delivery>) -> Unit,
    onEventClick: (Delivery) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .monthScroll(listState, coroutineScope)
    ) {
        itemsIndexed(weeks, key = { _, weekStart -> weekStart.toEpochDay() }) { _, weekStart ->
            WeekRow(
                startOfWeek = weekStart,
                deliveries = deliveries,
                onDayClick = onDayClick,
                onEventClick = onEventClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

fun generateWeeksAround(center: LocalDate, count: Int): List<LocalDate> {
    val start = center.with(DayOfWeek.MONDAY).minusWeeks(count / 2L)
    return List(count) { start.plusWeeks(it.toLong()) }
}