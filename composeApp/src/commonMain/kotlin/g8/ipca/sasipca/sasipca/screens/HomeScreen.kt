package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import g8.ipca.sasipca.sasipca.navigation.NavigationService
import g8.ipca.sasipca.sasipca.navigation.Screen
import g8.ipca.sasipca.sasipca.storage.SessionManager
import g8.ipca.sasipca.sasipca.ui.components.HeaderSection
import g8.ipca.sasipca.sasipca.utils.getCurrentMonthPt
import g8.ipca.sasipca.sasipca.utils.getFormattedDatePt
import g8.ipca.sasipca.sasipca.utils.getGreetingPt

@Composable
fun HomeScreen() {
    val userName = SessionManager.currentUserName ?: "Utilizador"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection("${getGreetingPt()}, $userName", getFormattedDatePt())

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
            icon = Icons.Filled.Inventory2,
            title = "Receção",
            modifier = Modifier.weight(1f),
            onClick = { NavigationService.navigateTo(Screen.Reception) }
        )

        QuickActionButton(
            icon = Icons.Filled.LocalShipping,
            title = "Entrega",
            modifier = Modifier.weight(1f),
            onClick = { NavigationService.navigateTo(Screen.Delivery) }
        )

        QuickActionButton(
            icon = Icons.Filled.Tune,
            title = "Ajuste",
            modifier = Modifier.weight(1f),
            onClick = { NavigationService.navigateTo(Screen.StockAdjustment) }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactStatCard(
                    icon = Icons.Outlined.Schedule,
                    label = "Pendentes",
                    value = "08",
                    modifier = Modifier.weight(1f)
                )
                CompactStatCard(
                    icon = Icons.Outlined.Favorite,
                    label = "Doações",
                    value = "15",
                    modifier = Modifier.weight(1f)
                )
                CompactStatCard(
                    icon = Icons.Outlined.CheckCircle,
                    label = "Realizadas",
                    value = "03",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CompactStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
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
            icon = Icons.Outlined.Person,
            title = "Beneficiários",
            onClick = { NavigationService.navigateTo(Screen.Beneficiaries) }
        )
        CompactMenuItem(
            icon = Icons.Outlined.Person,
            title = "Relatórios",
            onClick = { NavigationService.navigateTo(Screen.Placeholder) }
        )
    }
}

@Composable
fun CompactMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
