package sasipca.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import sasipca.repositories.StockRepository
import sasipca.viewmodels.DeliveriesViewModel
import sasipca.models.DeliveryCreationDTO
import sasipca.models.DeliveryUpdateDTO
import sasipca.models.VDeliveryDTO
import sasipca.ui.components.calendar.CalendarHeader
import sasipca.ui.components.calendar.Calendar
import sasipca.ui.components.calendar.WeekCalendarController
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(stockRepository: StockRepository) {
    val viewModel = remember { DeliveriesViewModel(stockRepository) }

    val month by viewModel.month.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var editorState by remember { mutableStateOf<VDeliveryDTO?>(null) }
    var pickerState by remember { mutableStateOf<Pair<LocalDate, List<VDeliveryDTO>>?>(null) }

    var showFutureDeliveries by remember { mutableStateOf(false) }

    LaunchedEffect(month) {
        viewModel.loadMonthDeliveries(month)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        val isCompact = maxWidth < 800.dp

        if (isCompact) {
            CompactLayout(
                month = month,
                selectedDate = selectedDate,
                deliveries = deliveries,
                viewModel = viewModel,
                pickerState = pickerState,
                onPickerStateChange = { pickerState = it },
                editorState = editorState,
                onEditorStateChange = { editorState = it },
                showFutureDeliveries = showFutureDeliveries,
                onMonthChange = { viewModel.selectMonth(it) },
                onShowFutureDeliveriesChange = { showFutureDeliveries = it }
            )
        } else {
            WideLayout(
                month = month,
                selectedDate = selectedDate,
                deliveries = deliveries,
                viewModel = viewModel,
                pickerState = pickerState,
                onPickerStateChange = { pickerState = it },
                editorState = editorState,
                onEditorStateChange = { editorState = it },
                onMonthChange = { viewModel.selectMonth(it) }
            )
        }


        pickerState?.let { (date, deliveriesForDate) ->
            EventPickerDialog(
                date = date,
                deliveries = deliveriesForDate,
                onDismiss = { pickerState = null },
                onSelect = { selected ->
                    pickerState = null
                    editorState = selected ?: VDeliveryDTO(
                        deliveryId = 0,
                        beneficiaryId = 0,
                        beneficiaryName = null,
                        scheduledDate = date.toString(),
                        status = "Agendada",
                        note = null,
                        userId = 0,
                        userName = null
                    )
                }
            )
        }

        editorState?.let { dto ->
            EventEditorDialog(
                initial = dto,
                onDismiss = { editorState = null },
                onDelete = {
                    // TODO: Implementar método de eliminação no ViewModel se necessário
                },
                onSave = { updated ->
                    if (updated.deliveryId == 0) {
                        viewModel.scheduleDelivery(
                            DeliveryCreationDTO(
                                beneficiaryId = updated.beneficiaryId,
                                scheduledDate = updated.scheduledDate, // já é String ISO
                                note = updated.note,
                                itemsToDeliver = emptyList() // TODO: substituir por itens reais quando disponível
                            )
                        )
                    } else {
                        // Atualizar entrega existente
                        viewModel.updateDelivery(
                            updated.deliveryId,
                            DeliveryUpdateDTO(
                                scheduledDate = updated.scheduledDate,
                                newStatus = when (updated.status.lowercase()) {
                                    "entregue" -> 2
                                    "cancelada" -> 3
                                    else -> 1
                                },
                                note = updated.note,
                                itemsToDeliver = emptyList() // TODO: substituir por itens reais quando disponível
                            )
                        )
                    }
                    editorState = null
                }
            )
        }

    }
}

@Composable
fun CompactLayout(
    month: YearMonth,
    selectedDate: LocalDate,
    deliveries: List<VDeliveryDTO>,
    viewModel: DeliveriesViewModel,
    pickerState: Pair<LocalDate, List<VDeliveryDTO>>?,
    onPickerStateChange: (Pair<LocalDate, List<VDeliveryDTO>>?) -> Unit,
    editorState: VDeliveryDTO?,
    onEditorStateChange: (VDeliveryDTO?) -> Unit,
    showFutureDeliveries: Boolean,
    onMonthChange: (YearMonth) -> Unit,
    onShowFutureDeliveriesChange: (Boolean) -> Unit
) {
    var calendarController by remember { mutableStateOf<WeekCalendarController?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader(
            month = month,
            onPrev = { calendarController?.scrollToPreviousMonth() },
            onNext = { calendarController?.scrollToNextMonth() },
            onToday = { calendarController?.scrollToToday() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                onEventClick = { onEditorStateChange(it) },
                deliveries,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            )
        } else {
            Calendar(
                month = month,
                startDate = selectedDate,
                deliveries = deliveries,
                onMonthChange = onMonthChange,
                onDayClick = { date, deliveriesForDate ->
                    viewModel.selectDate(date)
                    onPickerStateChange(date to deliveriesForDate)
                },
                onEventClick = { onEditorStateChange(it) },
                modifier = Modifier.weight(1f),
                controller = { calendarController = it }
            )
        }
    }
}

@Composable
fun WideLayout(
    month: YearMonth,
    selectedDate: LocalDate,
    deliveries: List<VDeliveryDTO>,
    viewModel: DeliveriesViewModel,
    pickerState: Pair<LocalDate, List<VDeliveryDTO>>?,
    onPickerStateChange: (Pair<LocalDate, List<VDeliveryDTO>>?) -> Unit,
    editorState: VDeliveryDTO?,
    onEditorStateChange: (VDeliveryDTO?) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    var calendarController by remember { mutableStateOf<WeekCalendarController?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader(
            month = month,
            onPrev = { calendarController?.scrollToPreviousMonth() },
            onNext = { calendarController?.scrollToNextMonth() },
            onToday = { calendarController?.scrollToToday() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Calendar(
                month = month,
                startDate = selectedDate,
                deliveries = deliveries,
                onMonthChange = onMonthChange,
                onDayClick = { date, deliveriesForDate ->
                    viewModel.selectDate(date)
                    onPickerStateChange(date to deliveriesForDate)
                },
                onEventClick = { onEditorStateChange(it) },
                modifier = Modifier.weight(1f),
                controller = { calendarController = it }
            )

            Spacer(modifier = Modifier.width(12.dp))

            FutureDeliveriesList(
                onEventClick = { onEditorStateChange(it) },
                deliveries,
                modifier = Modifier.width(280.dp).fillMaxHeight()
            )
        }
    }
}

@Composable
fun FutureDeliveriesList(
    onEventClick: (VDeliveryDTO) -> Unit,
    deliveries: List<VDeliveryDTO>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Entregas Futuras",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        if (deliveries.isEmpty()) {
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
        }
    }
}


// ----------------------------------------------------------
// Picker Dialog (entregas por data)
// ----------------------------------------------------------
@Composable
fun EventPickerDialog(
    date: LocalDate,
    deliveries: List<VDeliveryDTO>,
    onDismiss: () -> Unit,
    onSelect: (VDeliveryDTO?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Entregas em ${date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                if (deliveries.isEmpty()) {
                    Text("Sem entregas — criar nova?")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(deliveries) { dto ->
                            Surface(
                                tonalElevation = 1.dp,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(dto) }
                                    .padding(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        dto.beneficiaryName ?: "Entrega",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        dto.status ?: "Agendada",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                        Text("Nova Entrega")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorDialog(
    initial: VDeliveryDTO,
    onDismiss: () -> Unit,
    onSave: (VDeliveryDTO) -> Unit,
    onDelete: (VDeliveryDTO) -> Unit
) {
    var beneficiaryId by remember { mutableStateOf(initial.beneficiaryId.toString()) }
    var date by remember { mutableStateOf(initial.scheduledDate) }
    var note by remember { mutableStateOf(initial.note ?: "") }
    var status by remember { mutableStateOf(initial.status) }

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
                        text = if (initial.deliveryId == 0) "Nova Entrega" else "Editar Entrega",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )

                    if (initial.deliveryId != 0) {
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
                    value = beneficiaryId,
                    onValueChange = { beneficiaryId = it.filter(Char::isDigit) },
                    label = { Text("ID Beneficiário") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de estado textual (Agendada, Entregue, Cancelada)
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Estado") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val updated = initial.copy(
                            beneficiaryId = beneficiaryId.toIntOrNull() ?: 0,
                            scheduledDate = date,
                            note = note.ifBlank { null },
                            status = status
                        )
                        onSave(updated)
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
