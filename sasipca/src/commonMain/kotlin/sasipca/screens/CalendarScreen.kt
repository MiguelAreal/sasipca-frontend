package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import sasipca.models.Delivery
import sasipca.repositories.DeliveryRepository
import sasipca.viewmodels.DeliveriesViewModel
import sasipca.storage.ListsStore
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.ScreenSizeManager.isSmallScreen
import sasipca.ui.components.calendar.CalendarHeader
import sasipca.ui.components.calendar.Calendar
import sasipca.ui.components.calendar.WeekCalendarController
import java.time.LocalDate
import java.time.YearMonth
@Composable
fun CalendarScreen(
    deliveryRepository: DeliveryRepository,
    onNavigateToDelivery: (LocalDate?, Boolean, Delivery?) -> Unit
) {
    val deliveriesViewModel = remember { DeliveriesViewModel(deliveryRepository) }
    val month by deliveriesViewModel.month.collectAsState()
    val deliveries by deliveriesViewModel.deliveries.collectAsState()
    val futureDeliveries by deliveriesViewModel.futureDeliveries.collectAsState()
    val isLoading by deliveriesViewModel.isLoading.collectAsState()

    var pickerState by remember { mutableStateOf<Pair<LocalDate, List<Delivery>>?>(null) }
    var showFutureDeliveries by remember { mutableStateOf(false) }

    val scheduledStatusId = remember {
        ListsStore.DeliveriesStatus.find {
            it.status.equals("Agendada", ignoreCase = true)
        }?.id
    }

    LaunchedEffect(Unit) {
        deliveriesViewModel.loadFutureDeliveries()
    }

    LaunchedEffect(month) {
        deliveriesViewModel.loadMonthDeliveries(month)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        val safeNavigate: (Delivery) -> Unit = { delivery ->
            if (scheduledStatusId != null && delivery.statusId == scheduledStatusId) {
                onNavigateToDelivery(null, true, delivery)
            }
        }

        if (isSmallScreen()) {
            CompactLayout(
                month = month,
                deliveries = deliveries,
                futureDeliveries = futureDeliveries,
                onPickerStateChange = { pickerState = it },
                onEventClick = safeNavigate,
                showFutureDeliveries = showFutureDeliveries,
                onMonthChange = { deliveriesViewModel.selectMonth(it) },
                onShowFutureDeliveriesChange = { showFutureDeliveries = it }, // Corrigido: Agora atualiza o estado
                scheduledStatusId = scheduledStatusId
            )
        } else {
            WideLayout(
                month = month,
                deliveries = deliveries,
                futureDeliveries = futureDeliveries,
                onPickerStateChange = { pickerState = it },
                onEventClick = safeNavigate,
                onMonthChange = { deliveriesViewModel.selectMonth(it) },
                scheduledStatusId = scheduledStatusId
            )
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }

        // --- DIALOG ---
        pickerState?.let { (date, deliveriesForDate) ->
            EventPickerDialog(
                date = date,
                deliveries = deliveriesForDate,
                scheduledStatusId = scheduledStatusId,
                onDismiss = { pickerState = null }, // Corrigido: Agora fecha o dialog
                onSelectExisting = { selected ->
                    safeNavigate(selected)
                },
                onNewDelivery = {
                    onNavigateToDelivery(date, true, null)
                }
            )
        }
    }
}

@Composable
fun CompactLayout(
    month: YearMonth,
    deliveries: List<Delivery>,
    futureDeliveries: List<Delivery>,
    onPickerStateChange: (Pair<LocalDate, List<Delivery>>?) -> Unit,
    onEventClick: (Delivery) -> Unit,
    showFutureDeliveries: Boolean,
    onMonthChange: (YearMonth) -> Unit,
    onShowFutureDeliveriesChange: (Boolean) -> Unit,
    scheduledStatusId: Int?
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
                onEventClick = onEventClick,
                deliveries = futureDeliveries,
                scheduledStatusId = scheduledStatusId,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            )
        } else {
            Calendar(
                month = month,
                deliveries = deliveries,
                onMonthChange = onMonthChange,
                onDayClick = { date, deliveriesForDate ->
                    onPickerStateChange(date to deliveriesForDate)
                },
                onEventClick = onEventClick,
                modifier = Modifier.weight(1f),
                controller = { calendarController = it }
            )
        }
    }
}

@Composable
fun WideLayout(
    month: YearMonth,
    deliveries: List<Delivery>,
    futureDeliveries: List<Delivery>,
    onPickerStateChange: (Pair<LocalDate, List<Delivery>>?) -> Unit,
    onEventClick: (Delivery) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    scheduledStatusId: Int?
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
                deliveries = deliveries,
                onMonthChange = onMonthChange,
                onDayClick = { date, deliveriesForDate ->
                    onPickerStateChange(date to deliveriesForDate)
                },
                onEventClick = onEventClick,
                modifier = Modifier.weight(1f),
                controller = { calendarController = it }
            )

            Surface(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                FutureDeliveriesList(
                    onEventClick = onEventClick,
                    deliveries = futureDeliveries,
                    scheduledStatusId = scheduledStatusId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// --- LISTA FUTURA COM VERIFICAÇÃO DE "AGENDADA" ---

@Composable
fun FutureDeliveriesList(
    onEventClick: (Delivery) -> Unit,
    deliveries: List<Delivery>,
    scheduledStatusId: Int?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isLargeScreen()) {
            Text(
                text = "Próximas Entregas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        if (deliveries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem entregas agendadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(deliveries) { delivery ->
                    // Só é editável se statusId for igual a "Agendada"
                    val isEditable = scheduledStatusId != null && delivery.statusId == scheduledStatusId

                    ListItem(
                        modifier = Modifier
                            .clickable(enabled = isEditable) { onEventClick(delivery) },
                        headlineContent = {
                            Text(
                                text = delivery.beneficiaryName ?: "Sem Nome",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                color = if(isEditable) Color.Unspecified else Color.Gray
                            )
                        },
                        supportingContent = {
                            Text(
                                text = delivery.scheduledDate,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if(isEditable) MaterialTheme.colorScheme.primary else Color.Gray,
                                        CircleShape
                                    )
                            )
                        },
                        trailingContent = {
                            if (isEditable) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// --- DIALOG COM VERIFICAÇÃO DE "AGENDADA" ---

@Composable
fun EventPickerDialog(
    date: LocalDate,
    deliveries: List<Delivery>,
    scheduledStatusId: Int?,
    onDismiss: () -> Unit,
    onSelectExisting: (Delivery) -> Unit,
    onNewDelivery: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Entregas a ${date.dayOfMonth}/${date.monthValue}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))

                if (deliveries.isEmpty()) {
                    Text("Sem entregas para este dia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 250.dp)
                    ) {
                        items(deliveries) { dto ->
                            val isEditable = scheduledStatusId != null && dto.statusId == scheduledStatusId

                            Surface(
                                tonalElevation = 2.dp,
                                shape = RoundedCornerShape(8.dp),
                                enabled = isEditable, // Desabilita se não for Agendada
                                onClick = { onSelectExisting(dto) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dto.beneficiaryName ?: "Entrega",
                                        fontWeight = FontWeight.Medium,
                                        color = if(isEditable) Color.Unspecified else Color.Gray
                                    )
                                    // Se não for editável, mostra o estado real (ex: Cancelada, Entregue)
                                    if(!isEditable) {
                                        val statusName = ListsStore.getDeliveriesStatusName(dto.statusId)
                                        Text(
                                            text = "($statusName)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onNewDelivery) { Text("Nova Entrega") }
                }
            }
        }
    }
}