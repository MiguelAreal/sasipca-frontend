package sasipca.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sasipca.models.GroupToEnter
import sasipca.ui.components.products.GroupCard
import sasipca.ui.theme.CardTitle


@Composable
fun GroupsSection(
    groups: List<GroupToEnter>,
    onAddGroup: () -> Unit,
    onGroupChange: (Int, GroupToEnter) -> Unit,
    onRemoveGroup: (Int) -> Unit,
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
                        CardTitle("Grupos")
                        IconButton(onClick = onAddGroup) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Adicionar grupo"
                            )
                        }
                    }

                    // Groups List (Optimized for height - less padding)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes remaining height in the Card
                            .padding(horizontal = 8.dp), // Reduced horizontal padding
                        verticalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
                        contentPadding = PaddingValues(bottom = 8.dp) // Added bottom padding for last item
                    ) {
                        itemsIndexed(groups) { index, group ->
                            GroupCard(
                                group = group,
                                index = index,
                                onGroupChange = { onGroupChange(index, it) },
                                onRemove = { onRemoveGroup(index) },
                                canRemove = groups.size > 1,
                                errors = errors
                            )
                        }
                    }
                }
            }
        }
    }
}