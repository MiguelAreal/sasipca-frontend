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
import sasipca.utils.monthScroll
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
    month: YearMonth, // Mês que está a ser visualizado (foco)
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

    // Gera semanas suficientes para trás e para a frente
    val weeks = remember(startDate) { generateWeeksAround(startDate, 200) }

    val todayWeekIndex = remember(weeks) {
        weeks.indexOfFirst { weekStart ->
            val weekEnd = weekStart.plusDays(6)
            today in weekStart..weekEnd
        }.coerceAtLeast(0)
    }

    // Scroll inicial para hoje
    LaunchedEffect(Unit) {
        listState.scrollToItem(todayWeekIndex)
    }

    // Controlador externo
    LaunchedEffect(Unit) {
        controller(
            WeekCalendarController(
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
                scrollToToday = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(todayWeekIndex)
                    }
                }
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        WeekHeader(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Define altura da semana (aprox 5 semanas visíveis para parecer um mês)
            val visibleWeeks = 5.5f
            val weekHeight = maxHeight / visibleWeeks

            LazyColumn(
                state = listState,
                // Adiciona o suporte a scroll de rato (Desktop) e drag snap (Android)
                modifier = Modifier
                    .fillMaxSize()
                    .monthScroll(listState, coroutineScope)
            ) {
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
                        // Remove padding vertical para as linhas se tocarem (estilo grelha)
                    )
                }
            }
        }
    }

    // Deteta mudança de mês baseada no scroll
    val firstVisibleWeekIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LaunchedEffect(firstVisibleWeekIndex) {
        // Pega na semana do meio visível para determinar o mês "focado"
        val midIndex = (firstVisibleWeekIndex + 2).coerceIn(weeks.indices)
        val midWeekDate = weeks[midIndex].plusDays(3)
        val newMonth = YearMonth.from(midWeekDate)

        if (newMonth != month) {
            onMonthChange(newMonth)
        }
    }
}

fun generateWeeksAround(center: LocalDate, count: Int): List<LocalDate> {
    val start = center.with(DayOfWeek.MONDAY).minusWeeks(count / 2L)
    return List(count) { start.plusWeeks(it.toLong()) }
}