package sasipca.ui.components

import ValidatedTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.ActiveCampaigns
import sasipca.models.Category
import sasipca.models.UnitType
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.products.ProductImagesCarousel

@Composable
fun ReceiptInfoSection(
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
    selectedCampaign: ActiveCampaigns?,
    onCampaignSelect: (ActiveCampaigns?) -> Unit,
    campaigns: List<ActiveCampaigns>,
    note: String,
    onNoteChange: (String) -> Unit,
    errors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    if (isLargeScreen()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Informações do Produto
                Column(
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
                            ValidatedTextField(
                                value = productName,
                                onValueChange = onProductNameChange,
                                label = "Nome do Produto",
                                error = errors["name"],
                                maxLength = 255
                            )
                            ValidatedDropdown(
                                label = "Categoria",
                                items = categories,
                                selectedItem = selectedCategory,
                                onSelect = onCategorySelect,
                                error = errors["category"],
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ValidatedDropdown(
                                    label = "Unidade",
                                    items = units,
                                    selectedItem = selectedUnit,
                                    onSelect = onUnitSelect,
                                    error = errors["unit"],
                                    modifier = Modifier.weight(1f)
                                )
                                ValidatedTextField(
                                    value = unitSize,
                                    onValueChange = onUnitSizeChange,
                                    label = "Quantidade",
                                    error = errors["unitSize"],
                                    maxLength = 11,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }
                    }
                }

                // Associar Campanha
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Associar Campanha", fontSize = 16.sp)
                    ValidatedDropdown(
                        label = "Campanha",
                        items = campaigns,
                        selectedItem = selectedCampaign,
                        onSelect = onCampaignSelect,
                        error = errors["campaign"],
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Observações
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Observações", fontSize = 16.sp)
                    ValidatedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        label = "Quantidade",
                        error = errors["unitSize"],
                        maxLength = 300,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        label = { Text("Observações") },
                        placeholder = { Text("Adicione notas sobre a receção...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    ValidatedTextField(
                        value = productName,
                        onValueChange = onProductNameChange,
                        label = "Nome do Produto",
                        error = errors["name"],
                        maxLength = 255,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ValidatedDropdown(
                        label = "Categoria",
                        items = categories,
                        selectedItem = selectedCategory,
                        onSelect = onCategorySelect,
                        error = errors["category"],
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ValidatedDropdown(
                            label = "Unidade",
                            items = units,
                            selectedItem = selectedUnit,
                            onSelect = onUnitSelect,
                            error = errors["unit"],
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedTextField(
                            value = unitSize,
                            onValueChange = onUnitSizeChange,
                            label = "Quantidade",
                            error = errors["unitSize"],
                            maxLength = 11,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
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
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Associar Campanha", fontSize = 16.sp)
                    ValidatedDropdown(
                        label = "Campanha",
                        items = campaigns,
                        selectedItem = selectedCampaign,
                        onSelect = onCampaignSelect,
                        error = errors["campaign"],
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Observações", fontSize = 16.sp)
                    OutlinedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        label = { Text("Observações") },
                        placeholder = { Text("Adicione notas sobre a receção...") },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 5
                    )
                }
            }
        }
    }
}