package sasipca.ui.components.beneficiaries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.VDeliveryDTO


@Composable
fun DeliveryRow(
    delivery: VDeliveryDTO,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = delivery.scheduledDate ?: "N/A",
            modifier = Modifier.weight(0.35f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = delivery.userName ?: "N/A",
            modifier = Modifier.weight(0.35f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = delivery.status ?: "N/A",
            modifier = Modifier.weight(0.3f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

    }
}