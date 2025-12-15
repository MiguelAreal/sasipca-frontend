package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sasipca.repositories.StatsRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.charts.*
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.StatsViewModel
import sasipca.viewmodels.TimeRange
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(statsRepository: StatsRepository) {
    val viewModel = remember { StatsViewModel(statsRepository) }

    // Carregamento inicial
    LaunchedEffect(Unit) {
        viewModel.loadAllAdvancedStats()
    }

    // --- DATE PICKER DIALOG (Material 3) ---
    if (viewModel.showDatePicker) {
        val datePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = { viewModel.onCustomDatesSelected(null, null) },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = datePickerState.selectedStartDateMillis
                    val endMillis = datePickerState.selectedEndDateMillis

                    if (startMillis != null && endMillis != null) {
                        // Conversão segura de Millis para LocalDate
                        val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onCustomDatesSelected(start, end)
                    }
                }) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCustomDatesSelected(null, null) }) { Text("Cancelar") }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.height(500.dp), // Altura fixa para caber bem no ecrã
                title = { Text(text = "Selecione o intervalo", modifier = Modifier.padding(16.dp)) },
                headline = {
                    // Headline customizada opcional ou default
                    DateRangePickerDefaults.DateRangePickerHeadline(
                        selectedStartDateMillis = datePickerState.selectedStartDateMillis,
                        selectedEndDateMillis = datePickerState.selectedEndDateMillis,
                        displayMode = datePickerState.displayMode,
                        dateFormatter = DatePickerDefaults.dateFormatter(),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header("Dashboard Analítico", getFormattedDatePt())

        // --- BARRA DE FILTROS ---
        FilterBar(
            selectedRange = viewModel.selectedRange,
            onRangeSelected = { viewModel.setTimeRange(it) }
        )

        if (viewModel.isLoading) {
            LoadingWidget()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // 1. KPIS GLOBAIS
                viewModel.summary?.let { summary ->
                    if (isLargeScreen()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            KpiCard("Stock Total", summary.totalProductsInStock.toString(), Icons.Outlined.Inventory2, Modifier.weight(1f))
                            KpiCard("Stock Crítico", summary.lowStockCount.toString(), Icons.Outlined.Warning, Modifier.weight(1f), isWarning = true)
                            KpiCard("Pendentes", summary.pendingDeliveriesCount.toString(), Icons.Outlined.Schedule, Modifier.weight(1f))
                            KpiCard("Beneficiários", summary.activeBeneficiariesCount.toString(), Icons.Outlined.People, Modifier.weight(1f))
                        }
                    } else {
                        // Mobile Grid
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KpiCard("Stock Total", summary.totalProductsInStock.toString(), Icons.Outlined.Inventory2, Modifier.weight(1f))
                                KpiCard("Stock Crítico", summary.lowStockCount.toString(), Icons.Outlined.Warning, Modifier.weight(1f), isWarning = true)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KpiCard("Pendentes", summary.pendingDeliveriesCount.toString(), Icons.Outlined.Schedule, Modifier.weight(1f))
                                KpiCard("Beneficiários", summary.activeBeneficiariesCount.toString(), Icons.Outlined.People, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // 2. FLUXO DE STOCK (LINHA INTERATIVA)
                DashboardCard(title = "Evolução do Stock", icon = Icons.Outlined.Timeline) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        if (viewModel.movementsFlow.isNotEmpty()) {
                            InteractiveLineChart(
                                data = viewModel.movementsFlow,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else EmptyStateMessage()
                    }
                }

                // 3. SECÇÃO DIVIDIDA (DONUT + BARRAS)
                // Altura fixa para garantir que o donut e as barras têm espaço
                val chartContainerModifier = if (isLargeScreen()) Modifier.weight(1f).height(400.dp) else Modifier.fillMaxWidth()

                if (isLargeScreen()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        DashboardCard("Categorias (Saídas)", Icons.Outlined.PieChart, chartContainerModifier) {
                            if (viewModel.categoriesData.isNotEmpty())
                                InteractiveDonutChart(viewModel.categoriesData, Modifier.fillMaxSize())
                            else EmptyStateMessage()
                        }
                        DashboardCard("Top Produtos", Icons.Outlined.TrendingUp, chartContainerModifier) {
                            if (viewModel.topProducts.isNotEmpty())
                                InteractiveBarChart(viewModel.topProducts, Modifier.fillMaxSize())
                            else EmptyStateMessage()
                        }
                    }
                } else {
                    DashboardCard("Categorias (Saídas)", Icons.Outlined.PieChart) {
                        if (viewModel.categoriesData.isNotEmpty())
                            InteractiveDonutChart(viewModel.categoriesData, Modifier.fillMaxWidth())
                        else EmptyStateMessage()
                    }
                    DashboardCard("Top Produtos", Icons.Outlined.TrendingUp) {
                        if (viewModel.topProducts.isNotEmpty())
                            InteractiveBarChart(viewModel.topProducts, Modifier.fillMaxWidth())
                        else EmptyStateMessage()
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ------------------------------------------------------------------------
// SUB-COMPONENTES UI (Para garantir que não tens erros de referência)
// ------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.values().forEach { range ->
            val isSelected = range == selectedRange
            FilterChip(
                selected = isSelected,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val bgColor = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, null, tint = contentColor.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text("Sem dados para apresentar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}