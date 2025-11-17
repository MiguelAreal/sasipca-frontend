package sasipca.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.LotToEnter
import sasipca.models.ReceiptLotItem
import sasipca.ui.components.products.LotCard
import sasipca.ui.theme.CardTitle


@Composable
fun LotsSection(
    lots: List<LotToEnter>,
    onAddLot: () -> Unit,
    onLotChange: (Int, LotToEnter) -> Unit,
    onRemoveLot: (Int) -> Unit,
    isWideScreen: Boolean,
    errors: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    if (isWideScreen) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardTitle("Lotes")
                        IconButton(onClick = onAddLot) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Adicionar lote"
                            )
                        }
                    }

                    // Lots List (Optimized for height - less padding)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes remaining height in the Card
                            .padding(horizontal = 8.dp), // Reduced horizontal padding
                        verticalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
                        contentPadding = PaddingValues(bottom = 8.dp) // Added bottom padding for last item
                    ) {
                        itemsIndexed(lots) { index, lot ->
                            LotCard(
                                lot = lot,
                                index = index,
                                onLotChange = { onLotChange(index, it) },
                                onRemove = { onRemoveLot(index) },
                                canRemove = lots.size > 1,
                                errors = errors
                            )
                        }
                    }
                }
            }
        }
    }
}