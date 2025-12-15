package sasipca.ui.components.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import sasipca.models.ProductGroup
import sasipca.ui.theme.CardTitle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun ProductGroupsTable(
    groups: List<ProductGroup>
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            CardTitle("Stock por Validade")
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem stock registado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Validade", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("Qtd. Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("Disponível", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
                Divider()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Ordena diretamente (LocalDate suporta comparação)
                    items(groups.sortedBy { it.expiryDate }) { group ->
                        GroupRow(group)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun GroupRow(group: ProductGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val expiryDate = group.expiryDate

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val isExpired = expiryDate < today

        val day = expiryDate.day.toString().padStart(2, '0')
        val month = expiryDate.month.number.toString().padStart(2, '0')
        val year = expiryDate.year
        val dateStr = "$day-$month-$year"

        Text(
            text = dateStr,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = group.totalQuantity.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )

        Text(
            text = group.availableStock.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}