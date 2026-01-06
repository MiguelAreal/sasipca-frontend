package sasipca.ui.components.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.MovementHistory
import sasipca.models.getMovementTypeName
import sasipca.ui.theme.CardTitle
import sasipca.ui.components.LoadingWidget

// Opções de Ordenação
enum class HistorySortColumn {
    DATE, TYPE, USER, QUANTITY
}

enum class HistorySortDirection {
    ASCENDING, DESCENDING
}

@Composable
fun ProductHistoryTable(
    history: List<MovementHistory>,
    isLoading: Boolean
) {
    // Estados Locais
    var selectedMovement by remember { mutableStateOf<MovementHistory?>(null) }
    var sortColumn by remember { mutableStateOf(HistorySortColumn.DATE) }
    var sortDirection by remember { mutableStateOf(HistorySortDirection.DESCENDING) }

    // Lógica de Ordenação
    val sortedHistory = remember(history, sortColumn, sortDirection) {
        val sorted = when (sortColumn) {
            HistorySortColumn.DATE -> history.sortedBy { it.movementDate }
            HistorySortColumn.TYPE -> history.sortedBy { getMovementTypeName(it.movementTypeId) }
            HistorySortColumn.USER -> history.sortedBy { it.userName ?: "" }
            HistorySortColumn.QUANTITY -> history.sortedBy { it.totalQuantityAffected ?: 0.0 }
        }
        if (sortDirection == HistorySortDirection.DESCENDING) sorted.reversed() else sorted
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // Título
            CardTitle("Histórico de Movimentos")
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (history.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sem registo de movimentos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // CABEÇALHO DA TABELA
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HistoryTableHeader(
                                    text = "Data",
                                    column = HistorySortColumn.DATE,
                                    currentSort = sortColumn,
                                    direction = sortDirection,
                                    modifier = Modifier.weight(1.3f)
                                ) { col ->
                                    if (sortColumn == col) sortDirection = if (sortDirection == HistorySortDirection.ASCENDING) HistorySortDirection.DESCENDING else HistorySortDirection.ASCENDING
                                    else { sortColumn = col; sortDirection = HistorySortDirection.DESCENDING }
                                }

                                HistoryTableHeader(
                                    text = "Tipo",
                                    column = HistorySortColumn.TYPE,
                                    currentSort = sortColumn,
                                    direction = sortDirection,
                                    modifier = Modifier.weight(0.8f)
                                ) { col ->
                                    if (sortColumn == col) sortDirection = if (sortDirection == HistorySortDirection.ASCENDING) HistorySortDirection.DESCENDING else HistorySortDirection.ASCENDING
                                    else { sortColumn = col; sortDirection = HistorySortDirection.ASCENDING }
                                }

                                HistoryTableHeader(
                                    text = "Qtd.",
                                    column = HistorySortColumn.QUANTITY,
                                    currentSort = sortColumn,
                                    direction = sortDirection,
                                    modifier = Modifier.weight(0.7f)
                                ) { col ->
                                    if (sortColumn == col) sortDirection = if (sortDirection == HistorySortDirection.ASCENDING) HistorySortDirection.DESCENDING else HistorySortDirection.ASCENDING
                                    else { sortColumn = col; sortDirection = HistorySortDirection.DESCENDING }
                                }

                                HistoryTableHeader(
                                    text = "Utilizador",
                                    column = HistorySortColumn.USER,
                                    currentSort = sortColumn,
                                    direction = sortDirection,
                                    modifier = Modifier.weight(1f)
                                ) { col ->
                                    if (sortColumn == col) sortDirection = if (sortDirection == HistorySortDirection.ASCENDING) HistorySortDirection.DESCENDING else HistorySortDirection.ASCENDING
                                    else { sortColumn = col; sortDirection = HistorySortDirection.ASCENDING }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        // LINHAS DA TABELA
                        items(sortedHistory, key = { it.movementId }) { move ->
                            HistoryRow(
                                move = move,
                                isSelected = selectedMovement?.movementId == move.movementId,
                                onClick = { selectedMovement = move }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }

                if (isLoading) {
                    LoadingWidget()
                }
            }

            // PAINEL DE DETALHES (OBSERVAÇÕES)
            if (selectedMovement != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 200.dp),
                    shape = RoundedCornerShape(0.dp), // Retangular para colar ao fundo
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "Observações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val note = selectedMovement?.movementNote
                        Text(
                            text = if (note.isNullOrBlank()) "Sem observações" else note,
                            fontSize = 14.sp,
                            color = if (note.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun HistoryTableHeader(
    text: String,
    column: HistorySortColumn,
    currentSort: HistorySortColumn,
    direction: HistorySortDirection,
    modifier: Modifier = Modifier,
    onClick: (HistorySortColumn) -> Unit
) {
    Row(
        modifier = modifier.clickable { onClick(column) },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 13.sp, // Ligeiramente menor para caber
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (currentSort == column) {
            Icon(
                imageVector = if (direction == HistorySortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HistoryRow(
    move: MovementHistory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. DATA (Formatada)
        val dateStr = try {
            val iso = move.movementDate
            // yyyy-MM-ddTHH:mm:ss -> dd-MM-yyyy HH:mm
            val year = iso.take(4)
            val month = iso.substring(5, 7)
            val day = iso.substring(8, 10)
            val time = iso.substring(11, 16)
            "$day-$month-$year $time"
        } catch (_: Exception) { move.movementDate.take(10) }

        Text(
            text = dateStr,
            modifier = Modifier.weight(1.3f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 2. TIPO (Com Cor)
        val typeName = getMovementTypeName(move.movementTypeId)
        val typeColor = when (move.movementTypeId) {
            1 -> MaterialTheme.colorScheme.primary // Entrada
            2 -> MaterialTheme.colorScheme.error   // Saída
            3 -> MaterialTheme.colorScheme.tertiary // Ajuste
            else -> MaterialTheme.colorScheme.onSurface
        }

        Text(
            text = typeName,
            modifier = Modifier.weight(0.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = typeColor
        )

        // 3. QUANTIDADE (+/-)
        val qty = move.totalQuantityAffected ?: 0.0

        val qtyColor = if (move.movementTypeId == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        Text(
            text = qty.toString(),
            modifier = Modifier.weight(0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = qtyColor
        )

        // 4. UTILIZADOR
        Text(
            text = move.userName ?: "-",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}