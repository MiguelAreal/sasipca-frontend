package sasipca.ui.components.beneficiaries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.VDeliveryDTO
import sasipca.storage.ScreenSizeManager.isLargeScreen


enum class SortColumn {
    USERNAME, STATUS, SCHEDULED_DATE
}

enum class SortDirection {
    ASCENDING, DESCENDING
}


@Composable
fun DeliveriesTable(
    deliveries: List<VDeliveryDTO>,
    isLoading: Boolean
) {
    var selectedDelivery by remember { mutableStateOf<VDeliveryDTO?>(null) }
    var sortColumn by remember { mutableStateOf(SortColumn.SCHEDULED_DATE) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }

    val sortedDeliveries = remember(deliveries, sortColumn, sortDirection) {
        val sorted = when (sortColumn) {
            SortColumn.USERNAME -> deliveries.sortedBy { it.userName ?: "" }
            SortColumn.STATUS -> deliveries.sortedBy { it.statusId }
            SortColumn.SCHEDULED_DATE -> deliveries.sortedBy { it.scheduledDate }
        }
        if (sortDirection == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {

                Text(
                    "Histórico de Entregas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            }



            Box(modifier = Modifier.weight(1f)) {
                if (deliveries.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sem entregas registadas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Table Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DeliveriesTableHeader(
                                    text = "Data",
                                    sortColumn = SortColumn.SCHEDULED_DATE,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.35f),
                                    onClick = {
                                        if (sortColumn == SortColumn.SCHEDULED_DATE)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = SortColumn.SCHEDULED_DATE
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                                DeliveriesTableHeader(
                                    text = "Utilizador",
                                    sortColumn = SortColumn.USERNAME,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.35f),
                                    onClick = {
                                        if (sortColumn == SortColumn.USERNAME)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = SortColumn.USERNAME
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                                DeliveriesTableHeader(
                                    text = "Estado",
                                    sortColumn = SortColumn.STATUS,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.30f),
                                    onClick = {
                                        if (sortColumn == SortColumn.STATUS)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = SortColumn.STATUS
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )

                            }
                        }

                        items(sortedDeliveries) { delivery ->
                            DeliveryRow(
                                delivery = delivery,
                                isSelected = selectedDelivery?.deliveryId == delivery.deliveryId,
                                onClick = { selectedDelivery = delivery }
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (selectedDelivery != null) {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 200.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Observações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedDelivery?.note ?: "Sem observações",
                            fontSize = 14.sp,
                            color = selectedDelivery?.note?.let { MaterialTheme.colorScheme.onSurface } ?: MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}