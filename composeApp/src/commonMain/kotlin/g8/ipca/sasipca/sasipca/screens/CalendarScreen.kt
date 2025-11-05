package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import g8.ipca.sasipca.sasipca.ui.components.CalendarHeader
import g8.ipca.sasipca.sasipca.ui.components.monthScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// -------------------------
// Data models
// -------------------------

data class DeliveryItemDTO(
    val barcode: String,
    val lot: String,
    val quantity: Int
)

data class DeliveryCreationDTO(
    val beneficiaryId: Int,
    val scheduledDate: LocalDate,
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItemDTO> = emptyList()
)

enum class DeliveryStatus { Agendada, Entregue, Cancelada }

data class DeliveryUpdateDTO(
    val scheduledDate: LocalDate?,
    val newStatus: DeliveryStatus?,
    val note: String? = null,
    val itemsToDeliver: List<DeliveryItemDTO> = emptyList()
)

data class CalendarEvent(
    val id: Int? = null,
    val title: String,
    val date: LocalDate,
    val beneficiaryId: Int,
    val note: String? = null,
    val items: List<DeliveryItemDTO> = emptyList(),
    val status: DeliveryStatus = DeliveryStatus.Agendada
)

// -------------------------
// Repository (in-memory)
// -------------------------

object CalendarRepository {
    private var nextId = 1
    private val events = mutableStateListOf<CalendarEvent>()

    fun getEventsFor(month: YearMonth): List<CalendarEvent> =
        events.filter { YearMonth.from(it.date) == month }

    fun getEventsFor(date: LocalDate): List<CalendarEvent> =
        events.filter { it.date == date }

    fun getAllEvents(): List<CalendarEvent> = events.toList()

    fun addEvent(event: CalendarEvent): CalendarEvent {
        val ev = event.copy(id = nextId++)
        events += ev
        return ev
    }

    fun updateEvent(updated: CalendarEvent) {
        val idx = events.indexOfFirst { it.id == updated.id }
        if (idx >= 0) events[idx] = updated
    }

    fun deleteEvent(event: CalendarEvent) {
        event.id?.let { id -> events.removeAll { it.id == id } }
    }
}

// -------------------------
// Fake API Service
// -------------------------

object DeliveryApiService {
    suspend fun scheduleDelivery(dto: DeliveryCreationDTO): Result<String> =
        Result.success("Criado com sucesso (simulado)")

    suspend fun updateDelivery(deliveryId: Int, dto: DeliveryUpdateDTO): Result<String> =
        Result.success("Atualizado com sucesso (simulado)")
}

// -------------------------
// UI - Adaptado para Desktop
// -------------------------

@Composable
fun CalendarScreen() {
    val windowInfo = LocalWindowInfo.current
    val isCompact = windowInfo.containerSize.width < 800
    val isMedium = windowInfo.containerSize.width in 800..1200

    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var editorState by remember { mutableStateOf<CalendarEvent?>(null) }
    var pickerState by remember { mutableStateOf<Pair<LocalDate, List<CalendarEvent>>?>(null) }
    var showFutureDeliveries by remember { mutableStateOf(false) }

    if (isCompact) {
        CompactLayout(
            month = month,
            selectedDate = selectedDate,
            showFutureDeliveries = showFutureDeliveries,
            onMonthChange = { month = it },
            onShowFutureDeliveriesChange = { showFutureDeliveries = it },
            onDayClick = { date ->
                selectedDate = date
                val eventsForDate = CalendarRepository.getEventsFor(date)
                if (eventsForDate.isEmpty()) {
                    editorState = CalendarEvent(
                        title = "Nova Entrega",
                        date = date,
                        beneficiaryId = 0
                    )
                } else {
                    pickerState = date to eventsForDate
                }
            },
            onEventClick = { editorState = it }
        )
    } else {
        WideLayout(
            month = month,
            selectedDate = selectedDate,
            isTablet = isMedium,
            onMonthChange = { month = it },
            onDayClick = { date ->
                selectedDate = date
                val eventsForDate = CalendarRepository.getEventsFor(date)
                if (eventsForDate.isEmpty()) {
                    editorState = CalendarEvent(
                        title = "Nova Entrega",
                        date = date,
                        beneficiaryId = 0
                    )
                } else {
                    pickerState = date to eventsForDate
                }
            },
            onEventClick = { editorState = it }
        )
    }

    // Dialogs comuns
    pickerState?.let { (date, eventsForDate) ->
        EventPickerDialog(
            date = date,
            events = eventsForDate,
            onDismiss = { pickerState = null },
            onSelect = { ev ->
                pickerState = null
                editorState = ev ?: CalendarEvent(
                    title = "Nova Entrega",
                    date = date,
                    beneficiaryId = 0
                )
            }
        )
    }

    editorState?.let { current ->
        EventEditorDialog(
            initial = current,
            onDismiss = { editorState = null },
            onDelete = {
                CalendarRepository.deleteEvent(it)
                editorState = null
            },
            onSave = { ev ->
                if (ev.id == null) {
                    val created = CalendarRepository.addEvent(ev)
                    CoroutineScope(Dispatchers.IO).launch {
                        DeliveryApiService.scheduleDelivery(
                            DeliveryCreationDTO(ev.beneficiaryId, ev.date, ev.note, ev.items)
                        )
                    }
                } else {
                    CalendarRepository.updateEvent(ev)
                    CoroutineScope(Dispatchers.IO).launch {
                        DeliveryApiService.updateDelivery(
                            ev.id, DeliveryUpdateDTO(ev.date, ev.status, ev.note, ev.items)
                        )
                    }
                }
                editorState = null
            }
        )
    }
}

// Layout compacto para mobile
@Composable
fun CompactLayout(
    month: YearMonth,
    selectedDate: LocalDate,
    showFutureDeliveries: Boolean,
    onMonthChange: (YearMonth) -> Unit,
    onShowFutureDeliveriesChange: (Boolean) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (CalendarEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader(
            month = month,
            onPrev = { onMonthChange(month.minusMonths(1)) },
            onNext = { onMonthChange(month.plusMonths(1)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle entre calendário e entregas futuras
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !showFutureDeliveries,
                onClick = { onShowFutureDeliveriesChange(false) },
                label = { Text("Calendário") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = showFutureDeliveries,
                onClick = { onShowFutureDeliveriesChange(true) },
                label = { Text("Entregas Futuras") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showFutureDeliveries) {
            FutureDeliveriesList(
                onEventClick = onEventClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            )
        } else {
            MonthGrid(
                month = month,
                events = CalendarRepository.getEventsFor(month),
                onDayClick = onDayClick,
                onEventClick = onEventClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                onMonthChange = onMonthChange,
                isCompact = true
            )
        }

        // Botão "Hoje"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    onMonthChange(YearMonth.now())
                    onShowFutureDeliveriesChange(false)
                },
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Hoje", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Layout largo para tablet/desktop
@Composable
fun WideLayout(
    month: YearMonth,
    selectedDate: LocalDate,
    isTablet: Boolean,
    onMonthChange: (YearMonth) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (CalendarEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader(
            month = month,
            onPrev = { onMonthChange(month.minusMonths(1)) },
            onNext = { onMonthChange(month.plusMonths(1)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Calendário
            MonthGrid(
                month = month,
                events = CalendarRepository.getEventsFor(month),
                onDayClick = onDayClick,
                onEventClick = onEventClick,
                modifier = Modifier.weight(1f),
                onMonthChange = onMonthChange,
                isCompact = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Coluna lateral - ajusta largura conforme ecrã
            FutureDeliveriesList(
                onEventClick = onEventClick,
                modifier = Modifier
                    .width(if (isTablet) 280.dp else 320.dp)
                    .fillMaxHeight()
            )
        }

        // Botão "Hoje"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onMonthChange(YearMonth.now()) },
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Hoje", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Componente reutilizável para lista de entregas futuras
@Composable
fun FutureDeliveriesList(
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Entregas Futuras",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        val futureEvents by remember {
            derivedStateOf {
                CalendarRepository.getAllEvents()
                    .filter { it.date >= LocalDate.now() }
                    .sortedBy { it.date }
            }
        }

        if (futureEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sem entregas agendadas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                futureEvents.forEach { event ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onEventClick(event) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = event.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = event.date.format(
                                    DateTimeFormatter.ofPattern("dd MMM yyyy")
                                ),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthGrid(
    month: YearMonth,
    events: List<CalendarEvent>,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
    onMonthChange: (YearMonth) -> Unit = {},
    isCompact: Boolean = false
) {
    val firstOfMonth = month.atDay(1)
    val start = firstOfMonth.with(DayOfWeek.MONDAY)

    Column(modifier = modifier
        .fillMaxHeight()
        .monthScroll(month, onMonthChange)
    ) {
        // Cabeçalhos dos dias
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.values().forEach { dow ->
                val dayLabel = dow.getDisplayName(TextStyle.SHORT, Locale("pt", "PT"))
                    .replaceFirstChar { it.uppercase() } // capitaliza a primeira letra
                Text(
                    text = if (isCompact) dayLabel.first().toString() else dayLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grelha de dias
        Column {
            var cellDate = start
            for (row in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val date = cellDate
                        val isCurrentMonth = YearMonth.from(date) == month
                        val eventsForDay = events.filter { it.date == date }
                        DayCell(
                            date = date,
                            isCurrentMonth = isCurrentMonth,
                            events = eventsForDay,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompact) 70.dp else 100.dp),
                            onClick = { onDayClick(date) },
                            isCompact = isCompact
                        )
                        cellDate = cellDate.plusDays(1)
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val hasEvents = events.isNotEmpty()
    val isToday = date == LocalDate.now()

    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.secondaryContainer
        hasEvents -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
        hasEvents -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> if (isCurrentMonth) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Card(
        shape = RoundedCornerShape(if (isCompact) 6.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .padding(if (isCompact) 2.dp else 4.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 4.dp else 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = if (isCompact) 12.sp else 14.sp
            )

            if (hasEvents && !isCompact) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${events.size} entrega${if (events.size > 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = textColor
                )
            } else if (hasEvents && isCompact) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "●",
                    fontSize = 16.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun EventPickerDialog(
    date: LocalDate,
    events: List<CalendarEvent>,
    onDismiss: () -> Unit,
    onSelect: (CalendarEvent?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Eventos em ${date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                if (events.isEmpty()) {
                    Text("Sem eventos — criar novo?")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(events) { ev ->
                            Surface(
                                tonalElevation = 1.dp,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(ev) }
                                    .padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(ev.title, fontWeight = FontWeight.Medium)
                                        ev.note?.let {
                                            Text(it, fontSize = 12.sp, maxLines = 1)
                                        }
                                    }
                                    Text(ev.status.name, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSelect(null) }) {
                        Text("Novo Evento")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorDialog(
    initial: CalendarEvent,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var beneficiaryId by remember { mutableStateOf(initial.beneficiaryId.toString()) }
    var date by remember { mutableStateOf(initial.date) }
    var note by remember { mutableStateOf(initial.note ?: "") }
    var status by remember { mutableStateOf(initial.status) }

    val items = remember {
        mutableStateListOf<DeliveryItemDTO>().apply { addAll(initial.items) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (initial.id == null) "Nova Entrega" else "Editar Entrega",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )

                    if (initial.id != null) {
                        IconButton(
                            onClick = { onDelete(initial) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar entrega"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = beneficiaryId,
                    onValueChange = { beneficiaryId = it.filter { ch -> ch.isDigit() } },
                    label = { Text("ID Beneficiário") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date.toString(),
                    onValueChange = {
                        runCatching { LocalDate.parse(it) }.onSuccess { date = it }
                    },
                    label = { Text("Data (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Itens", fontWeight = FontWeight.Medium)
                items.forEachIndexed { idx, it ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "${it.barcode} / ${it.lot} x ${it.quantity}",
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { items.removeAt(idx) }) {
                            Text("Remover")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { items += DeliveryItemDTO("BC123", "L1", 1) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adicionar Item (exemplo)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val bid = beneficiaryId.toIntOrNull() ?: 0
                        val ev = initial.copy(
                            title = title,
                            beneficiaryId = bid,
                            date = date,
                            note = if (note.isBlank()) null else note,
                            items = items.toList(),
                            status = status
                        )
                        onSave(ev)
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
