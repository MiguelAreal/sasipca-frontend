package sasipca.ui.components.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import sasipca.models.ProductGroup
import sasipca.ui.theme.CardTitle
import kotlin.time.Clock

@Composable
fun ProductGroupsTable(
    groups: List<ProductGroup>,
    isBeneficiary: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            CardTitle("Stock por Validade")
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem stock registado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Validade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // SÓ MOSTRA SE NÃO FOR BENEFICIÁRIO
                    if (!isBeneficiary) {
                        Text(
                            text = "Qtd. Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Disponível",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(groups.sortedBy { it.expiryDate }) { group ->
                        GroupRow(group, isBeneficiary)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun GroupRow(
    group: ProductGroup,
    isBeneficiary: Boolean
) {
    val expiryDate = group.expiryDate
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isExpired = expiryDate < today

    // Definir cor de fundo baseada na validade
    val rowBackground = if (isExpired) {
        MaterialTheme.colorScheme.errorContainer // Vermelho suave do tema
    } else {
        Color.Transparent
    }

    // Definir cor do texto se estiver expirado (para garantir contraste)
    val textColor = if (isExpired) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground) // Aplica o fundo vermelho se expirado
            .padding(vertical = 12.dp, horizontal = 4.dp), // Padding extra-horizontal para não colar
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Formatação manual da data
        val day = expiryDate.day.toString().padStart(2, '0')
        val month = expiryDate.month.number.toString().padStart(2, '0')
        val year = expiryDate.year
        val dateStr = "$day-$month-$year"

        // COLUNA 1: Validade
        Text(
            text = dateStr,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
        )

        // COLUNA 2: Total (Escondida para Beneficiário)
        if (!isBeneficiary) {
            Text(
                text = group.totalQuantity.toString(),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = textColor
            )
        }

        // COLUNA 3: Disponível
        Text(
            text = group.availableStock.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpired) textColor else MaterialTheme.colorScheme.primary
        )
    }
}