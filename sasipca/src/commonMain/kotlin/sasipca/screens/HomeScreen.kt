package sasipca.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sasipca.models.DashboardSummary
import sasipca.models.MonthlySummary
import sasipca.repositories.StatsRepository
import sasipca.screens.navigation.*
import sasipca.storage.SessionManager
import sasipca.ui.components.CompactMenuItem
import sasipca.ui.components.CompactStatCard
import sasipca.ui.components.Header
import sasipca.ui.components.QuickActionButton
import sasipca.utils.convertMonthPt
import sasipca.utils.getFormattedDatePt
import sasipca.utils.getGreetingPt
import sasipca.viewmodels.StatsViewModel
import java.time.YearMonth

@Composable
fun HomeScreen(
    statsRepository: StatsRepository
) {
    val userName = SessionManager.getUserName() ?: "Utilizador"
    val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

    // Inicializa o ViewModel
    val viewModel = remember { StatsViewModel(statsRepository) }

    // Carrega dados (Mensais + Sumário Global) ao iniciar
    LaunchedEffect(Unit) {
        viewModel.loadHomeStats()         // Carrossel mensal
        viewModel.loadAllAdvancedStats()  // KPIs globais (Expirados, Stock Total)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Header("${getGreetingPt()}, $userName", getFormattedDatePt())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))


            // 2. AÇÕES RÁPIDAS
            Text(
                text = "Ações Rápidas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            QuickActionsSection()

            Spacer(modifier = Modifier.height(24.dp))

            // 3. PERFORMANCE MENSAL (CARROSSEL)
            MonthlyCarouselSection(
                currentMonth = viewModel.currentHomeMonth,
                stats = viewModel.monthlyStats,
                isLoading = viewModel.isHomeLoading,
                onPrevClick = { viewModel.prevMonth() },
                onNextClick = { viewModel.nextMonth() },
                onDetailsClick = {
                    // Passamos o repositório para o ecrã de detalhes
                    navigator.push(StatsScreen())
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. MAIS OPÇÕES
            Text(
                text = "Mais Opções",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            SecondaryActionsSection()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickActionsSection() {
    val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Filled.ArrowCircleDown,
            title = "Receção",
            modifier = Modifier.weight(1f),
            onClick = { navigator.push(ReceptionScreen()) }
        )

        QuickActionButton(
            icon = Icons.Filled.ArrowCircleUp,
            title = "Entrega",
            modifier = Modifier.weight(1f),
            onClick = { navigator.push(DeliveryScreen()) }
        )

        QuickActionButton(
            icon = Icons.Filled.SwapHoriz,
            title = "Ajuste",
            modifier = Modifier.weight(1f),
            onClick = { navigator.push(StockAdjustmentScreen()) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyCarouselSection(
    currentMonth: YearMonth,
    stats: MonthlySummary?,
    isLoading: Boolean,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            // --- HEADER: NAVEGAÇÃO DE MÊS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Mês anterior",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = convertMonthPt(currentMonth.monthValue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentMonth.year.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onNextClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Próximo mês",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTEÚDO DOS DADOS ---
            AnimatedContent(
                targetState = stats,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "StatsFadeAnimation"
            ) { targetStats ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else if (targetStats != null) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 3
                    ) {
                        // 1. Entregas Pendentes (Neste mês)
                        CompactStatCard(
                            icon = Icons.Outlined.Schedule,
                            label = "Pendentes",
                            value = targetStats.pendingDeliveries.toString(),
                            modifier = Modifier.weight(1f)
                        )

                        // 2. Receções (Entradas)
                        CompactStatCard(
                            icon = Icons.Outlined.VolunteerActivism,
                            label = "Receções",
                            value = targetStats.donationsReceived.toString(),
                            modifier = Modifier.weight(1f)
                        )

                        // 3. Entregas Realizadas
                        CompactStatCard(
                            icon = Icons.Outlined.CheckCircle,
                            label = "Entregas",
                            value = targetStats.realizedDeliveries.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Sem dados disponíveis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- BOTÃO PARA VER MAIS DETALHES ---
            OutlinedButton(
                onClick = onDetailsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver Estatísticas Detalhadas")
            }
        }
    }
}

@Composable
fun SecondaryActionsSection() {
    val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactMenuItem(
            icon = Icons.Outlined.Campaign,
            title = "Campanhas",
            onClick = { navigator.push(CampaignsScreen()) }
        )
        CompactMenuItem(
            icon = Icons.Outlined.FilePresent,
            title = "Relatórios",
            onClick = { navigator.push(ReportsScreen()) }
        )
        CompactMenuItem(
            icon = Icons.Outlined.History,
            title = "Histórico de Movimentos",
            onClick = { navigator.push(HistoryScreen()) }
        )
        CompactMenuItem(
            icon = Icons.Outlined.SupervisorAccount,
            title = "Administradores",
            onClick = { navigator.push(AdminsScreen()) }
        )
    }
}