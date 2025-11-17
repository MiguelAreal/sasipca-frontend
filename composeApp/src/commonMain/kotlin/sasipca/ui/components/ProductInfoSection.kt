package sasipca.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.Category
import sasipca.models.UnitType
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.products.ProductImagesCarousel

@Composable
fun ProductInfoSection(
    productName: String,
    onProductNameChange: (String) -> Unit,
    images: List<String>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit,
    categories: List<Category>,
    selectedUnit: UnitType?,
    onUnitSelect: (UnitType?) -> Unit,
    units: List<UnitType>,
    unitSize: String,
    onUnitSizeChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryList = remember { mutableStateListOf(*categories.toTypedArray()) }
    val unitList = remember { mutableStateListOf(*units.toTypedArray()) }

    if (isLargeScreen()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Informações do Produto", fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (images.isNotEmpty()) {
                            ProductImagesCarousel(
                                images = images,
                                modifier = Modifier.weight(1f).height(250.dp)
                            )
                        }
                        Column(
                            modifier = if (images.isNotEmpty()) Modifier.weight(2f) else Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = productName,
                                onValueChange = onProductNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nome do Produto") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownSelector(
                                label = "Categoria",
                                items = categories,
                                selectedItem = selectedCategory,
                                onSelect = onCategorySelect,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DropdownSelector(
                                    label = "Unidade",
                                    items = units,
                                    selectedItem = selectedUnit,
                                    onSelect = onUnitSelect,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unitSize,
                                    onValueChange = onUnitSizeChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Quantidade") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Observações", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = onNoteChange,
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            placeholder = { Text("Adicione notas sobre a receção...") },
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 5
                        )
                    }
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Informações do Produto", fontSize = 16.sp)
                    if (images.isNotEmpty()) {
                        ProductImagesCarousel(
                            images = images,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                    OutlinedTextField(
                        value = productName,
                        onValueChange = onProductNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nome do Produto") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownSelector(
                        label = "Categoria",
                        items = categories,
                        selectedItem = selectedCategory,
                        onSelect = onCategorySelect,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DropdownSelector(
                            label = "Unidade",
                            items = units,
                            selectedItem = selectedUnit,
                            onSelect = onUnitSelect,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitSize,
                            onValueChange = onUnitSizeChange,
                            modifier = Modifier.weight(1f),
                            label = { Text("Quantidade") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Observações", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Adicione notas sobre a receção...") },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 5
                    )
                }
            }
        }
    }
}