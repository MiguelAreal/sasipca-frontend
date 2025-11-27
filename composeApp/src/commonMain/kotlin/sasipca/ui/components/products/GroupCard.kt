package sasipca.ui.components.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import sasipca.models.GroupToEnter
import sasipca.ui.components.ValidatedDateField
import sasipca.ui.components.ValidatedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: GroupToEnter,
    index: Int,
    onGroupChange: (GroupToEnter) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
    errors: Map<String, String> = emptyMap()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                val title = group.expiryDate.ifBlank {
                    "Grupo ${index + 1}"
                }

                Text(text = title, style = MaterialTheme.typography.titleSmall)

                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Remover grupo",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Quantidade (apenas números, máx 11 caracteres)
                ValidatedTextField(
                    value = group.quantity,
                    onValueChange = { onGroupChange(group.copy(quantity = it)) },
                    label = "Quant.",
                    maxLength = 11,
                    keyboardType = KeyboardType.Number,
                    error = errors["group_$index.quantity"],
                    modifier = Modifier.weight(1f)
                )
            }

            // Data de validade
            ValidatedDateField(
                value = group.expiryDate,
                onValueChange = { onGroupChange(group.copy(expiryDate = it)) },
                label = "Data de Validade",
                error = errors["group_$index.expiryDate"],
                modifier = Modifier.fillMaxWidth()
            )


        }
    }
}