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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.*
import sasipca.repositories.HistoryRepository
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.viewmodels.HistoryViewModel

@Composable
fun HistoryScreen(historyRepository: HistoryRepository) {
    val viewModel = remember { HistoryViewModel(historyRepository) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Header("Histórico", "Movimentos e Entregas")

        // --- TABS (Movimentos vs Entregas) ---
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
                text = { Text("Entregas Realizadas") }
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

            // Floating Refresh Button
            FloatingActionButton(
                onClick = { viewModel.loadData() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = Color.White)
            }

            if (viewModel.isLoading) {
                LoadingWidget()
            }
        }
    }

    // --- DIÁLOGOS DE DETALHE ---
    if (viewModel.selectedMovementDetail != null) {
        MovementDetailDialog(
            detail = viewModel.selectedMovementDetail!!,
            onDismiss = { viewModel.closeDialog() }
        )
    }

    if (viewModel.selectedDeliveryDetail != null) {
        DeliveryDetailDialog(
            detail = viewModel.selectedDeliveryDetail!!,
            onDismiss = { viewModel.closeDialog() }
        )
    }
}

// --------------------------------------------------------
// TABELA DE MOVIMENTOS
// --------------------------------------------------------
@Composable
fun MovementsTable(movements: List<MovementHistory>, onRowClick: (Int) -> Unit) {
    Column {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            Text("Data", Modifier.weight(0.3f), fontWeight = FontWeight.Bold)
            Text("Tipo", Modifier.weight(0.2f), fontWeight = FontWeight.Bold)
            Text("Utilizador", Modifier.weight(0.25f), fontWeight = FontWeight.Bold)
            Text("Total", Modifier.weight(0.15f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        }
        Divider()

        if (movements.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sem movimentos.") }
        } else {
            LazyColumn {
                items(movements) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRowClick(item.movementId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.movementDate.replace("T", " ").take(16), Modifier.weight(0.3f), fontSize = 14.sp)

                        // Badge para o Tipo
                        val color = when(item.movementTypeId) {
                            1 -> Color(0xFF2E7D32) // Entrada (Verde)
                            2 -> Color(0xFFD32F2F) // Saída (Vermelho)
                            else -> Color(0xFFF9A825) // Ajuste (Amarelo)
                        }
                        Text(
                            getMovementTypeName(item.movementTypeId),
                            Modifier.weight(0.2f),
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Text(item.userName ?: "-", Modifier.weight(0.25f), fontSize = 14.sp)
                        Text(
                            "${item.totalQuantityAffected ?: 0}",
                            Modifier.weight(0.15f),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            Text("Data Agend.", Modifier.weight(0.25f), fontWeight = FontWeight.Bold)
            Text("Beneficiário", Modifier.weight(0.35f), fontWeight = FontWeight.Bold)
            Text("Estado", Modifier.weight(0.2f), fontWeight = FontWeight.Bold)
            Text("Criador", Modifier.weight(0.2f), fontWeight = FontWeight.Bold)
        }
        Divider()

        if (deliveries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sem entregas.") }
        } else {
            LazyColumn {
                items(deliveries) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRowClick(item.deliveryId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.scheduledDate, Modifier.weight(0.25f), fontSize = 14.sp)
                        Text(item.beneficiaryName, Modifier.weight(0.35f), fontSize = 14.sp)

                        val color = when(item.statusId) {
                            2 -> Color(0xFF2E7D32) // Entregue
                            3 -> Color(0xFFD32F2F) // Cancelada
                            else -> Color.Gray
                        }
                        Text(getDeliveryStatusName(item.statusId), Modifier.weight(0.2f), color = color, fontSize = 14.sp)
                        Text(item.userName ?: "-", Modifier.weight(0.2f), fontSize = 14.sp)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// --------------------------------------------------------
// DIÁLOGO DETALHE MOVIMENTO
// --------------------------------------------------------
@Composable
fun MovementDetailDialog(detail: MovementDetail, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Detalhe Movimento #${detail.movementId}", fontWeight = FontWeight.Bold)
                Text(
                    "${getMovementTypeName(detail.movementTypeId)} - ${detail.movementDate.replace("T", " ").take(16)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (!detail.movementNote.isNullOrBlank()) {
                    Text("Nota: ${detail.movementNote}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (detail.deliveryId != null) {
                    Text("Associado à Entrega #${detail.deliveryId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                Text("Produtos Afetados:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                detail.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Validade: ${item.groupExpiryDate})", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "${if(item.itemQuantityAffected > 0) "+" else ""}${item.itemQuantityAffected}",
                            fontWeight = FontWeight.Bold,
                            color = if(item.itemQuantityAffected > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 2.dp), color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

// --------------------------------------------------------
// DIÁLOGO DETALHE ENTREGA
// --------------------------------------------------------
@Composable
fun DeliveryDetailDialog(detail: DeliveryDetail, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Detalhe Entrega #${detail.deliveryId}", fontWeight = FontWeight.Bold)
                Text(
                    "Para: ${detail.beneficiaryName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Data Agendada: ${detail.scheduledDate}")
                Text("Estado: ${getDeliveryStatusName(detail.statusId)}")
                if (!detail.note.isNullOrBlank()) {
                    Text("Nota: ${detail.note}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                Text("Itens Entregues:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                detail.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // O backend não manda nome do produto no DeliveryItemDTO,
                            // mas manda Barcode. Para ser perfeito precisarias de fazer lookup,
                            // mas aqui mostramos o Barcode.
                            Text("Item #${item.barcode}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Grupo #${item.groupId}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "${item.quantity} uni.",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 2.dp), color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}