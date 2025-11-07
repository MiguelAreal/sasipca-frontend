package sasipca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.VDeliveryDTO
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth


class WeekCalendarController(
    val scrollToNextMonth: () -> Unit,
    val scrollToPreviousMonth: () -> Unit,
    val scrollToToday: () -> Unit
)


@Composable
fun WeekCalendar(
    month: YearMonth,
    startDate: LocalDate = LocalDate.now(),
    deliveries: List<VDeliveryDTO> = emptyList(),
    onMonthChange: (YearMonth) -> Unit,
    onDayClick: (LocalDate, List<VDeliveryDTO>) -> Unit,
    onEventClick: (VDeliveryDTO) -> Unit,
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
        WeekDaysHeader(modifier = Modifier.padding(horizontal = 8.dp))

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
}@Composable
fun WeekRow(
    startOfWeek: LocalDate,
    focusedMonth: YearMonth,
    deliveries: List<VDeliveryDTO>,
    onDayClick: (LocalDate, List<VDeliveryDTO>) -> Unit,
    onEventClick: (VDeliveryDTO) -> Unit,
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
                    DayCellContent(
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


@Composable
fun DayCellContent(
    day: LocalDate,
    deliveriesForDay: List<VDeliveryDTO>,
    textColor: Color,
    onEventClick: (VDeliveryDTO) -> Unit,
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



@Composable
fun WeekDaysHeader(modifier: Modifier = Modifier) {
    val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (day in days) {
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}