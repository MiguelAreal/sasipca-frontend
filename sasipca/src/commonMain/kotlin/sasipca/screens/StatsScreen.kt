package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sasipca.repositories.StatsRepository
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.charts.*
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.StatsViewModel
import sasipca.viewmodels.TimeRange
import java.time.Instant
import java.time.ZoneId

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(statsRepository: StatsRepository) {
    val viewModel = remember { StatsViewModel(statsRepository) }

    // Carregamento inicial
    LaunchedEffect(Unit) {
        viewModel.loadAllAdvancedStats()
    }

    // --- DATE PICKER DIALOG (Adaptável a Landscape) ---
    if (viewModel.showDatePicker) {
        val datePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.onCustomDatesSelected(null, null) },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = datePickerState.selectedStartDateMillis
                    val endMillis = datePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
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
            // BoxWithConstraints garante que o calendário cabe em ecrãs baixos (landscape)
            BoxWithConstraints {
                val height = if (maxHeight < 500.dp) 350.dp else 500.dp
                DateRangePicker(
                    state = datePickerState,
                    modifier = Modifier.height(height),
                    title = { Text(text = "Selecione o intervalo", modifier = Modifier.padding(16.dp)) },
                    headline = {
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
    }

    // --- LAYOUT PRINCIPAL RESPONSIVO ---
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header("Dashboard Analítico", getFormattedDatePt())

        // Barra de filtros com scroll horizontal (LazyRow)
        FilterBar(
            selectedRange = viewModel.selectedRange,
            onRangeSelected = { viewModel.setTimeRange(it) }
        )

        if (viewModel.isLoading) {
            LoadingWidget()
        } else {
            // BoxWithConstraints lê a largura disponível para decidir o layout
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenWidth = maxWidth

                // Breakpoint para Desktop/Tablet Landscape
                val isWideScreen = screenWidth > 840.dp

                val contentPadding = if (isWideScreen) 32.dp else 16.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // 1. KPIS GLOBAIS (Grid Flexível)
                    viewModel.summary?.let { summary ->
                        ResponsiveKpiGrid(
                            screenWidth = screenWidth,
                            items = listOf(
                                Triple("Stock Total", summary.totalProductsInStock.toString(), Icons.Outlined.Inventory2),
                                // Stock Expirado com aviso visual
                                Triple("Stock Expirado", summary.expiredStockQuantity.toString(), Icons.Outlined.EventBusy),
                                Triple("Entregas Pendentes", summary.pendingDeliveriesCount.toString(), Icons.Outlined.Schedule),
                                Triple("Novos Beneficiários", summary.newBeneficiariesCount.toString(), Icons.Outlined.PersonAdd)
                            )
                        )
                    }

                    // 2. FLUXO DE STOCK (Gráfico de Linhas)
                    val chartHeight = if (isWideScreen) 400.dp else 300.dp
                    DashboardCard(title = "Fluxo de Stock (Tempo)", icon = Icons.Outlined.Timeline) {
                        Box(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                            if (viewModel.movementsFlow.isNotEmpty()) {
                                InteractiveLineChart(
                                    data = viewModel.movementsFlow,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else EmptyStateMessage()
                        }
                    }

                    // 3. SECÇÃO DE GRÁFICOS (ENTRADAS, SAÍDAS, TOP PRODUTOS)
                    if (isWideScreen) {
                        // --- LAYOUT DESKTOP ---

                        // Linha 1: Dois Donuts lado a lado
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.height(IntrinsicSize.Min) // Garante altura igual
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

                        // Linha 2: Top Produtos (Largura total)
                        DashboardCard("Top Produtos Entregues", Icons.Outlined.TrendingUp) {
                            Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                                if (viewModel.topProducts.isNotEmpty())
                                    InteractiveBarChart(viewModel.topProducts, Modifier.fillMaxSize())
                                else EmptyStateMessage()
                            }
                        }

                    } else {
                        // --- LAYOUT MOBILE ---
                        // Tudo empilhado verticalmente

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

                        DashboardCard("Top Produtos Entregues", Icons.Outlined.TrendingUp) {
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
}

// ------------------------------------------------------------------------
// COMPONENTES AUXILIARES DE UI & LAYOUT
// ------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    // LazyRow permite scroll infinito horizontal e evita cortes em ecrãs pequenos
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimeRange.entries.toTypedArray()) { range ->
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
fun ResponsiveKpiGrid(
    screenWidth: Dp,
    items: List<Triple<String, String, ImageVector>>
) {
    // Label que ativa o estilo de aviso (vermelho)
    val warningLabel = "Stock Expirado"

    when {
        // Desktop Wide: 1 linha com 4 colunas
        screenWidth > 840.dp -> {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items.forEach { (label, value, icon) ->
                    KpiCard(label, value, icon, Modifier.weight(1f), isWarning = label == warningLabel)
                }
            }
        }
        // Tablet: 2 linhas com 2 colunas
        screenWidth > 500.dp -> {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items.take(2).forEach { (label, value, icon) ->
                        KpiCard(label, value, icon, Modifier.weight(1f), isWarning = label == warningLabel)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items.takeLast(2).forEach { (label, value, icon) ->
                        KpiCard(label, value, icon, Modifier.weight(1f), isWarning = label == warningLabel)
                    }
                }
            }
        }
        // Mobile: 4 linhas (1 coluna) ou grid 2x2 apertada
        else -> {
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