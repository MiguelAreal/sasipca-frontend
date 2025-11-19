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
import sasipca.models.Delivery
import sasipca.models.DeliveryPost
import sasipca.models.DeliveryPut
import sasipca.repositories.DeliveryRepository
import sasipca.viewmodels.DeliveriesViewModel
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.ScreenSizeManager.isSmallScreen
import sasipca.ui.components.calendar.CalendarHeader
import sasipca.ui.components.calendar.Calendar
import sasipca.ui.components.calendar.WeekCalendarController
import sasipca.utils.getFormattedDatePt
import java.time.LocalDate
import java.time.YearMonth

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun CalendarScreen(deliveryRepository: DeliveryRepository) {
    val deliveriesViewModel = remember { DeliveriesViewModel(deliveryRepository) }
    val month by deliveriesViewModel.month.collectAsState()
    val selectedDate by deliveriesViewModel.selectedDate.collectAsState()
    val deliveries by deliveriesViewModel.deliveries.collectAsState()
    val futureDeliveries by deliveriesViewModel.futureDeliveries.collectAsState() // <--- NOVO
    val isLoading by deliveriesViewModel.isLoading.collectAsState()

    var editorState by remember { mutableStateOf<Delivery?>(null) }
    var pickerState by remember { mutableStateOf<Pair<LocalDate, List<Delivery>>?>(null) }

    var showFutureDeliveries by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        deliveriesViewModel.loadFutureDeliveries()
    }

    LaunchedEffect(month) {
        deliveriesViewModel.loadMonthDeliveries(month)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }


        if (isSmallScreen()) {
            CompactLayout(
                month = month,
                selectedDate = selectedDate,
                deliveries = deliveries,
                futureDeliveries = futureDeliveries,
                viewModel = deliveriesViewModel,
                pickerState = pickerState,
                onPickerStateChange = { pickerState = it },
                editorState = editorState,
                onEditorStateChange = { editorState = it },
                showFutureDeliveries = showFutureDeliveries,
                onMonthChange = { deliveriesViewModel.selectMonth(it) },
                onShowFutureDeliveriesChange = { showFutureDeliveries = it }
            )
        } else {
            WideLayout(
                month = month,
                selectedDate = selectedDate,
                deliveries = deliveries,
                futureDeliveries = futureDeliveries,
                viewModel = deliveriesViewModel,
                pickerState = pickerState,
                onPickerStateChange = { pickerState = it },
                editorState = editorState,
                onEditorStateChange = { editorState = it },
                onMonthChange = { deliveriesViewModel.selectMonth(it) }
            )
        }


        pickerState?.let { (date, deliveriesForDate) ->
            EventPickerDialog(
                date = date,
                deliveries = deliveriesForDate,
                onDismiss = { pickerState = null },
                onSelect = { selected ->
                    pickerState = null
                    editorState = selected ?: Delivery(
                        deliveryId = 0,
                        beneficiaryId = 0,
                        beneficiaryName = null,
                        scheduledDate = date.toString(),
                        statusId = 1,
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
                        deliveriesViewModel.scheduleDelivery(
                            DeliveryPost(
                                beneficiaryId = updated.beneficiaryId,
                                scheduledDate = updated.scheduledDate, // já é String ISO
                                note = updated.note,
                                itemsToDeliver = emptyList() // TODO: substituir por itens reais quando disponível
                            ),
                            true
                        )
                    } else {
                        // Atualizar entrega existente
                        deliveriesViewModel.updateDelivery(
                            updated.deliveryId,
                            DeliveryPut(
                                scheduledDate = updated.scheduledDate,
                                newStatusId = updated.statusId,
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
    deliveries: List<Delivery>,
    futureDeliveries: List<Delivery>,
    viewModel: DeliveriesViewModel,
    pickerState: Pair<LocalDate, List<Delivery>>?,
    onPickerStateChange: (Pair<LocalDate, List<Delivery>>?) -> Unit,
    editorState: Delivery?,
    onEditorStateChange: (Delivery?) -> Unit,
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
                futureDeliveries,
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
    deliveries: List<Delivery>,
    futureDeliveries: List<Delivery>,
    viewModel: DeliveriesViewModel,
    pickerState: Pair<LocalDate, List<Delivery>>?,
    onPickerStateChange: (Pair<LocalDate, List<Delivery>>?) -> Unit,
    editorState: Delivery?,
    onEditorStateChange: (Delivery?) -> Unit,
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
                futureDeliveries,
                modifier = Modifier.width(280.dp).fillMaxHeight()
            )
        }
    }
}


@Composable
fun FutureDeliveriesList(
    onEventClick: (Delivery) -> Unit,
    deliveries: List<Delivery>, // Esta lista deve ser futureDeliveries do ViewModel
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Título apenas para ecrãs grandes (lógica existente)
        if (isLargeScreen()){
            Text(
                text = "Entregas Futuras",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        if (deliveries.isEmpty()) {
            // Mensagem de lista vazia (lógica existente)
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
            // 🚀 Implementação para mostrar as entregas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                items(deliveries) { delivery ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clickable { onEventClick(delivery) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Nome do Beneficiário
                            Text(
                                text = delivery.beneficiaryName ?: "Entrega Agendada",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            // Data Agendada
                            Text(
                                text = "Data: ${delivery.scheduledDate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Nota (opcional)
                            if (delivery.note.isNullOrBlank().not()) {
                                Text(
                                    text = "Nota: ${delivery.note}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
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
    deliveries: List<Delivery>,
    onDismiss: () -> Unit,
    onSelect: (Delivery?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Entregas em ${getFormattedDatePt()}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                if (deliveries.isEmpty()) {
                    Text("Sem entregas - Criar nova?")
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
                                        dto.statusId.toString() ?: "Agendada",
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
    initial: Delivery,
    onDismiss: () -> Unit,
    onSave: (Delivery) -> Unit,
    onDelete: (Delivery) -> Unit
) {
    var beneficiaryId by remember { mutableStateOf(initial.beneficiaryId.toString()) }
    var date by remember { mutableStateOf(initial.scheduledDate) }
    var note by remember { mutableStateOf(initial.note ?: "") }
    var statusId by remember { mutableStateOf(initial.statusId) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.5f)
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

                    // Se estivermos a editar uma entrega, mostra botão de eliminar.
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
                    value = statusId.toString(),
                    onValueChange = { input ->
                        statusId = input.filter(Char::isDigit).toIntOrNull() ?: statusId
                    },
                    label = { Text("Estado (ID)") },
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
                            statusId = statusId
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
