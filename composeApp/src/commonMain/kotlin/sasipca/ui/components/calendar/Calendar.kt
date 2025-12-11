package sasipca.ui.components.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sasipca.models.Delivery
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth


class WeekCalendarController(
    val scrollToNextMonth: () -> Unit,
    val scrollToPreviousMonth: () -> Unit,
    val scrollToToday: () -> Unit
)


@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun Calendar(
    month: YearMonth,
    startDate: LocalDate = LocalDate.now(),
    deliveries: List<Delivery> = emptyList(),
    onMonthChange: (YearMonth) -> Unit,
    onDayClick: (LocalDate, List<Delivery>) -> Unit,
    onEventClick: (Delivery) -> Unit,
    modifier: Modifier = Modifier,
    controller: (WeekCalendarController) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val today = LocalDate.now()

    // 52 semanas fixas (~12 meses)
    val weeks = remember(startDate) { generateWeeksAround(startDate, 52) }

    val todayWeekIndex = remember(weeks) {
        weeks.indexOfFirst { weekStart ->
            val weekEnd = weekStart.plusDays(6)
            today in weekStart..weekEnd
        }.coerceAtLeast(0)
    }

    // Scroll inicial para semana atual
    LaunchedEffect(todayWeekIndex) {
        if (todayWeekIndex in weeks.indices) {
            listState.scrollToItem(todayWeekIndex)
        }
    }

    // Controlador externo
    LaunchedEffect(Unit) {
        controller(
            WeekCalendarController(
                scrollToNextMonth = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            (listState.firstVisibleItemIndex + 4).coerceAtMost(weeks.lastIndex)
                        )
                    }
                },
                scrollToPreviousMonth = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            (listState.firstVisibleItemIndex - 4).coerceAtLeast(0)
                        )
                    }
                },
                scrollToToday = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(todayWeekIndex)
                    }
                }
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        WeekHeader(modifier = Modifier.padding(horizontal = 8.dp))

        // Limita altura da LazyColumn e calcula altura de cada semana
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val weekHeight = maxHeight / 6 // altura fixa por semana, 6 linhas visíveis

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(weeks, key = { _, weekStart -> weekStart.toEpochDay() }) { _, weekStart ->
                    WeekRow(
                        startOfWeek = weekStart,
                        focusedMonth = month,
                        deliveries = deliveries,
                        onDayClick = onDayClick,
                        onEventClick = onEventClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(weekHeight)
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }

    // Atualiza mês ao scrollar
    val firstVisibleWeek by remember {
        derivedStateOf { weeks.getOrNull(listState.firstVisibleItemIndex) }
    }
    LaunchedEffect(firstVisibleWeek) {
        firstVisibleWeek?.let {
            val midWeek = it.plusDays(3)
            val newMonth = YearMonth.from(midWeek)
            if (newMonth != month) onMonthChange(newMonth)
        }
    }
}



fun generateWeeksAround(center: LocalDate, count: Int): List<LocalDate> {
    val start = center.with(DayOfWeek.MONDAY).minusWeeks(count / 2L)
    return List(count) { start.plusWeeks(it.toLong()) }
}





