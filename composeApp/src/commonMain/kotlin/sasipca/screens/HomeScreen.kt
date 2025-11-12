package sasipca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen
import sasipca.storage.SessionManager
import sasipca.ui.components.CompactMenuItem
import sasipca.ui.components.CompactStatCard
import sasipca.ui.components.Header
import sasipca.ui.components.QuickActionButton
import sasipca.utils.getCurrentMonthPt
import sasipca.utils.getFormattedDatePt
import sasipca.utils.getGreetingPt

@Composable
fun HomeScreen() {
    val userName = SessionManager.getUserName() ?: "Utilizador"

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

            Text(
                text = "Ações Rápidas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            QuickActionsSection()

            Spacer(modifier = Modifier.height(24.dp))

            MonthlyStatsSection()

            Spacer(modifier = Modifier.height(24.dp))

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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Filled.ArrowCircleDown,
            title = "Receção",
            modifier = Modifier.weight(1f),
            onClick = { NavigationService.navigateTo(Screen.Reception) }
        )

        QuickActionButton(
            icon = Icons.Filled.ArrowCircleUp,
            title = "Entrega",
            modifier = Modifier.weight(1f),
            onClick = { NavigationService.navigateTo(Screen.Delivery) }
        )

        QuickActionButton(
            icon = Icons.Filled.SwapHoriz,
            title = "Ajuste",
            modifier = Modifier.weight(1f),
            onClick = { NavigationService.navigateTo(Screen.StockAdjustment) }
        )
    }
}

@Composable
fun MonthlyStatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estatísticas de ${getCurrentMonthPt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = { /* TODO: Ver detalhes */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = "Ver detalhes",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ FlowRow para ajuste automático em ecrãs pequenos
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 3 // Máximo de 3 cards por linha
            ) {
                CompactStatCard(
                    icon = Icons.Outlined.Schedule,
                    label = "Pendentes",
                    value = "08"
                )
                CompactStatCard(
                    icon = Icons.Outlined.Favorite,
                    label = "Doações",
                    value = "15"
                )
                CompactStatCard(
                    icon = Icons.Outlined.CheckCircle,
                    label = "Realizadas",
                    value = "03"
                )
            }
        }
    }
}

@Composable
fun SecondaryActionsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactMenuItem(
            icon = Icons.Outlined.Campaign,
            title = "Campanhas",
            onClick = { NavigationService.navigateTo(Screen.Campaigns) }
        )
        CompactMenuItem(
            icon = Icons.Outlined.FilePresent,
            title = "Relatórios",
            onClick = { NavigationService.navigateTo(Screen.Placeholder) }
        )
        CompactMenuItem(
            icon = Icons.Outlined.History,
            title = "Histórico de Movimentos",
            onClick = { NavigationService.navigateTo(Screen.Placeholder) }
        )
    }
}

