package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.*
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.HistoryRepository
import sasipca.repositories.ReportsRepository
import sasipca.ui.components.Header
import sasipca.ui.components.LinearLoadingWidget // IMPORTAR O NOVO COMPONENTE
import sasipca.ui.components.ReportCreationPopup
import sasipca.utils.PlatformFileSaver
import sasipca.utils.SnackbarManager
import sasipca.viewmodels.BeneficiariesViewModel
import sasipca.viewmodels.HistoryViewModel
import sasipca.viewmodels.ReportsViewModel

// Enums para Ordenação Local
enum class SortDirection { ASCENDING, DESCENDING }
enum class MovSort { DATE, TYPE, USER, TOTAL }
enum class DelSort { DATE, BENEFICIARY, STATUS, USER }

@Composable
fun HistoryScreen(
    historyRepository: HistoryRepository,
    reportsRepository: ReportsRepository,
    beneficiaryRepository: BeneficiaryRepository
) {
    val viewModel = remember { HistoryViewModel(historyRepository) }
    val reportsViewModel = remember { ReportsViewModel(reportsRepository, PlatformFileSaver()) }
    val beneficiariesViewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }

    var showReportDialogForMovementId by remember { mutableStateOf<Int?>(null) }
    val reportUiState by reportsViewModel.uiState.collectAsState()

    // Gestão de Snackbars
    LaunchedEffect(reportUiState.error) {
        reportUiState.error?.let {
            SnackbarManager.show(it, SnackbarType.ERROR)
            reportsViewModel.clearMessages()
        }
    }
    LaunchedEffect(reportUiState.successMessage) {
        reportUiState.successMessage?.let {
            SnackbarManager.show(it, SnackbarType.SUCCESS)
            reportsViewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Header("Histórico", "Movimentos e Entregas")

        if (viewModel.isLoading || reportUiState.isLoading) {
            LinearLoadingWidget()
        }

        // --- TABS ---
        TabRow(
            selectedTabIndex = viewModel.currentTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = viewModel.currentTab == HistoryTab.MOVEMENTS,
                onClick = { viewModel.switchTab(HistoryTab.MOVEMENTS) },
                text = { Text("Movimentos de Stock") }
            )
            Tab(
                selected = viewModel.currentTab == HistoryTab.DELIVERIES,
                onClick = { viewModel.switchTab(HistoryTab.DELIVERIES) },
                text = { Text("Entregas") }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // CONTEÚDO DA TABELA
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                // Conteúdo interno com padding
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    if (viewModel.currentTab == HistoryTab.MOVEMENTS) {
                        MovementsTable(
                            movements = viewModel.movementsList,
                            onRowClick = { viewModel.openMovementDetails(it) }
                        )
                    } else {
                        DeliveriesHistoryTable(
                            deliveries = viewModel.deliveriesList,
                            onRowClick = { viewModel.openDeliveryDetails(it) }
                        )
                    }
                }
            }

            // Floating Refresh Button
            FloatingActionButton(
                onClick = { viewModel.loadData() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = Color.White)
            }

            // Removi o LoadingWidget antigo daqui pois agora está no topo
        }
    }

    // --- DIÁLOGOS E POPUPS ---
    if (viewModel.selectedMovementDetail != null) {
        MovementDetailDialog(
            detail = viewModel.selectedMovementDetail!!,
            onDismiss = { viewModel.closeDialog() },
            onPrintClick = { movementId -> showReportDialogForMovementId = movementId }
        )
    }

    if (viewModel.selectedDeliveryDetail != null) {
        DeliveryDetailDialog(
            detail = viewModel.selectedDeliveryDetail!!,
            onDismiss = { viewModel.closeDialog() }
        )
    }

    if (showReportDialogForMovementId != null) {
        ReportCreationPopup(
            beneficiariesViewModel = beneficiariesViewModel,
            onDismiss = { showReportDialogForMovementId = null },
            presetMovementId = showReportDialogForMovementId,
            onGenerate = { type, format, name, start, end, movId, status, beneId ->
                reportsViewModel.generateNewReport(type, format, name, start, end, movId, status, beneId)
                showReportDialogForMovementId = null
            }
        )
    }
}

// --------------------------------------------------------
// TABELA DE MOVIMENTOS
// --------------------------------------------------------
@Composable
fun MovementsTable(movements: List<MovementHistory>, onRowClick: (Int) -> Unit) {
    var sortColumn by remember { mutableStateOf(MovSort.DATE) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }

    val sortedList = remember(movements, sortColumn, sortDirection) {
        val sorted = when (sortColumn) {
            MovSort.DATE -> movements.sortedBy { it.movementDate }
            MovSort.TYPE -> movements.sortedBy { getMovementTypeName(it.movementTypeId) }
            MovSort.USER -> movements.sortedBy { it.userName ?: "" }
            // CORREÇÃO MANTIDA: .toDouble() para evitar o erro de compilação
            MovSort.TOTAL -> movements.sortedBy { it.totalQuantityAffected?.toDouble() ?: 0.0 }
        }
        if (sortDirection == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SortableTableHeader("Data", MovSort.DATE, sortColumn, sortDirection, Modifier.weight(1.3f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.DESCENDING }
            }
            SortableTableHeader("Tipo", MovSort.TYPE, sortColumn, sortDirection, Modifier.weight(0.8f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.ASCENDING }
            }
            SortableTableHeader("Utilizador", MovSort.USER, sortColumn, sortDirection, Modifier.weight(1f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.ASCENDING }
            }
            SortableTableHeader("Qtd.", MovSort.TOTAL, sortColumn, sortDirection, Modifier.weight(0.7f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.DESCENDING }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (movements.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem movimentos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(sortedList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRowClick(item.movementId) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(formatDate(item.movementDate), Modifier.weight(1.3f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                        val typeColor = when(item.movementTypeId) {
                            1 -> Color(0xFF2E7D32)
                            2 -> Color(0xFFD32F2F)
                            else -> Color(0xFFF9A825)
                        }
                        Text(
                            getMovementTypeName(item.movementTypeId),
                            Modifier.weight(0.8f),
                            color = typeColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Text(item.userName ?: "-", Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                        val qtyColor = if (item.totalQuantityAffected != null && item.totalQuantityAffected!! < 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                        Text(
                            "${item.totalQuantityAffected ?: 0}",
                            Modifier.weight(0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = qtyColor
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

// --------------------------------------------------------
// TABELA DE ENTREGAS
// --------------------------------------------------------
@Composable
fun DeliveriesHistoryTable(deliveries: List<DeliveryHistory>, onRowClick: (Int) -> Unit) {
    var sortColumn by remember { mutableStateOf(DelSort.DATE) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }

    val sortedList = remember(deliveries, sortColumn, sortDirection) {
        val sorted = when (sortColumn) {
            DelSort.DATE -> deliveries.sortedBy { it.scheduledDate }
            DelSort.BENEFICIARY -> deliveries.sortedBy { it.beneficiaryName }
            DelSort.STATUS -> deliveries.sortedBy { getDeliveryStatusName(it.statusId) }
            DelSort.USER -> deliveries.sortedBy { it.userName ?: "" }
        }
        if (sortDirection == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SortableTableHeader("Data", DelSort.DATE, sortColumn, sortDirection, Modifier.weight(1f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.DESCENDING }
            }
            SortableTableHeader("Beneficiário", DelSort.BENEFICIARY, sortColumn, sortDirection, Modifier.weight(1.3f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.ASCENDING }
            }
            SortableTableHeader("Estado", DelSort.STATUS, sortColumn, sortDirection, Modifier.weight(0.8f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.ASCENDING }
            }
            SortableTableHeader("Criador", DelSort.USER, sortColumn, sortDirection, Modifier.weight(0.8f)) {
                if (sortColumn == it) sortDirection = toggleSort(sortDirection) else { sortColumn = it; sortDirection = SortDirection.ASCENDING }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (deliveries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem entregas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(sortedList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRowClick(item.deliveryId) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.scheduledDate, Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(item.beneficiaryName, Modifier.weight(1.3f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)

                        val color = when(item.statusId) {
                            2 -> Color(0xFF2E7D32) // Entregue
                            3 -> Color(0xFFD32F2F) // Cancelada
                            else -> Color.Gray
                        }
                        Text(getDeliveryStatusName(item.statusId), Modifier.weight(0.8f), color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(item.userName ?: "-", Modifier.weight(0.8f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

// --------------------------------------------------------
// COMPONENTES AUXILIARES
// --------------------------------------------------------

@Composable
fun <T> SortableTableHeader(
    text: String,
    column: T,
    currentSort: T,
    direction: SortDirection,
    modifier: Modifier = Modifier,
    onClick: (T) -> Unit
) {
    Row(
        modifier = modifier.clickable { onClick(column) },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (currentSort == column) {
            Icon(
                imageVector = if (direction == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun toggleSort(current: SortDirection): SortDirection {
    return if (current == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
}

fun formatDate(iso: String): String {
    return try {
        // yyyy-MM-ddTHH:mm:ss -> dd-MM-yyyy HH:mm
        val year = iso.substring(0, 4)
        val month = iso.substring(5, 7)
        val day = iso.substring(8, 10)
        val time = iso.substring(11, 16)
        "$day-$month-$year $time"
    } catch (e: Exception) { iso.take(10) }
}

// --------------------------------------------------------
// DIÁLOGOS
// --------------------------------------------------------
@Composable
fun MovementDetailDialog(
    detail: MovementDetail,
    onDismiss: () -> Unit,
    onPrintClick: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Detalhe de Movimento", fontWeight = FontWeight.Bold)
                    Text(
                        "${getMovementTypeName(detail.movementTypeId)} - ${formatDate(detail.movementDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = { onPrintClick(detail.movementId) }) {
                    Icon(Icons.Default.FileDownload, "Relatório", tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (!detail.movementNote.isNullOrBlank()) {
                    Text("Nota: ${detail.movementNote}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }


                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("Produtos:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                detail.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Validade: ${item.groupExpiryDate}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "${if(item.itemQuantityAffected > 0) "+" else ""}${item.itemQuantityAffected}",
                            fontWeight = FontWeight.Bold,
                            color = if(item.itemQuantityAffected > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun DeliveryDetailDialog(detail: DeliveryDetail, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Detalhes de Entrega", fontWeight = FontWeight.Bold)
                Text("Para: ${detail.beneficiaryName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Data Agendada: ${detail.scheduledDate}")
                Text("Estado: ${getDeliveryStatusName(detail.statusId)}")
                if (!detail.note.isNullOrBlank()) Text("Nota: ${detail.note}", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("Itens:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                detail.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Validade: ${item.expiryDate}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("${item.quantity} uni.", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}