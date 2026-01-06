package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sasipca.repositories.StatsRepository
import sasipca.ui.components.Header
import sasipca.ui.components.InteractiveBarChart
import sasipca.ui.components.InteractiveDonutChart
import sasipca.ui.components.InteractiveLineChart
import sasipca.ui.components.LoadingWidget
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.StatsViewModel
import sasipca.viewmodels.TimeRange
import sasipca.storage.ScreenSizeManager.isLargeScreen
import java.time.Instant
import java.time.ZoneId

@Composable
fun StatsScreen(statsRepository: StatsRepository) {
    val viewModel = remember { StatsViewModel(statsRepository) }
    val isWide = isLargeScreen()

    // Carregamento inicial
    LaunchedEffect(Unit) {
        viewModel.loadAllAdvancedStats()
    }

    // --- DATE PICKER DIALOG ---
    if (viewModel.showDatePicker) {
        DatePickerModal(viewModel)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header("Dashboard Analítico", getFormattedDatePt())

        FilterBar(
            selectedRange = viewModel.selectedRange,
            onRangeSelected = { viewModel.setTimeRange(it) }
        )

        if (viewModel.isLoading) {
            LoadingWidget()
        } else {
            // Layout principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(if (isWide) 32.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. KPIS GLOBAIS
                viewModel.summary?.let { summary ->
                    ResponsiveKpiGrid(
                        isWide = isWide,
                        items = listOf(
                            Triple("Stock Total", summary.totalProductsInStock.toString(), Icons.Outlined.Inventory2),
                            Triple("Stock Expirado", summary.expiredStockQuantity.toString(), Icons.Outlined.EventBusy),
                            Triple("Entregas Pendentes", summary.pendingDeliveriesCount.toString(), Icons.Outlined.Schedule),
                            Triple("Novos Beneficiários", summary.newBeneficiariesCount.toString(), Icons.Outlined.PersonAdd)
                        )
                    )
                }

                // 2. FLUXO DE STOCK (Gráfico de Linhas)
                DashboardCard(title = "Fluxo de Stock (Tempo)", icon = Icons.Outlined.Timeline) {
                    Box(modifier = Modifier.fillMaxWidth().height(if (isWide) 400.dp else 300.dp)) {
                        if (viewModel.movementsFlow.isNotEmpty()) {
                            InteractiveLineChart(
                                data = viewModel.movementsFlow,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else EmptyStateMessage()
                    }
                }

                // 3. SECÇÃO DE GRÁFICOS (ENTRADAS, SAÍDAS, TOP PRODUTOS)
                StatsChartsSection(isWide, viewModel)

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// --- SUB-COMPOSABLE PARA LIMPEZA DE METADADOS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(viewModel: StatsViewModel) {
    val datePickerState = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = { viewModel.onCustomDatesSelected(null, null) },
        confirmButton = {
            TextButton(onClick = {
                val start = datePickerState.selectedStartDateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                val end = datePickerState.selectedEndDateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                if (start != null && end != null) viewModel.onCustomDatesSelected(start, end)
            }) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onCustomDatesSelected(null, null) }) { Text("Cancelar") }
        }
    ) {
        DateRangePicker(
            state = datePickerState,
            modifier = Modifier.height(500.dp),
            title = { Text("Selecione o intervalo", modifier = Modifier.padding(16.dp)) }
        )
    }
}

@Composable
private fun StatsChartsSection(isWide: Boolean, viewModel: StatsViewModel) {
    if (isWide) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                DashboardCard("Entradas por Categoria", Icons.Outlined.DonutLarge, Modifier.weight(1f).fillMaxHeight()) {
                    if (viewModel.categoriesDataIn.isNotEmpty())
                        InteractiveDonutChart(viewModel.categoriesDataIn, Modifier.fillMaxSize())
                    else EmptyStateMessage()
                }

                DashboardCard("Saídas por Categoria", Icons.Outlined.PieChart, Modifier.weight(1f).fillMaxHeight()) {
                    if (viewModel.categoriesDataOut.isNotEmpty())
                        InteractiveDonutChart(viewModel.categoriesDataOut, Modifier.fillMaxSize())
                    else EmptyStateMessage()
                }
            }

            DashboardCard("Top Produtos Entregues", Icons.AutoMirrored.Outlined.TrendingUp) {
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    if (viewModel.topProducts.isNotEmpty())
                        InteractiveBarChart(viewModel.topProducts, Modifier.fillMaxSize())
                    else EmptyStateMessage()
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            DashboardCard("Entradas por Categoria", Icons.Outlined.DonutLarge) {
                if (viewModel.categoriesDataIn.isNotEmpty())
                    InteractiveDonutChart(viewModel.categoriesDataIn, Modifier.fillMaxWidth().height(300.dp))
                else EmptyStateMessage()
            }

            DashboardCard("Saídas por Categoria", Icons.Outlined.PieChart) {
                if (viewModel.categoriesDataOut.isNotEmpty())
                    InteractiveDonutChart(viewModel.categoriesDataOut, Modifier.fillMaxWidth().height(300.dp))
                else EmptyStateMessage()
            }

            DashboardCard("Top Produtos Entregues", Icons.AutoMirrored.Outlined.TrendingUp) {
                if (viewModel.topProducts.isNotEmpty())
                    InteractiveBarChart(viewModel.topProducts, Modifier.fillMaxWidth().height(300.dp))
                else EmptyStateMessage()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimeRange.entries.toTypedArray()) { range ->
            FilterChip(
                selected = range == selectedRange,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) }
            )
        }
    }
}

@Composable
fun ResponsiveKpiGrid(isWide: Boolean, items: List<Triple<String, String, ImageVector>>) {
    val warningLabel = "Stock Expirado"
    if (isWide) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items.forEach { (label, value, icon) ->
                KpiCard(label, value, icon, Modifier.weight(1f), isWarning = label == warningLabel)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items.take(2).forEach { (label, value, icon) ->
                    KpiCard(label, value, icon, Modifier.weight(1f), isWarning = label == warningLabel)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items.takeLast(2).forEach { (label, value, icon) ->
                    KpiCard(label, value, icon, Modifier.weight(1f), isWarning = label == warningLabel)
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun KpiCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, isWarning: Boolean = false) {
    val bgColor = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface

    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Icon(icon, null, tint = contentColor.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text("Sem dados para apresentar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}