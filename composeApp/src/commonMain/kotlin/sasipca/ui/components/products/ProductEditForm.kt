    package sasipca.ui.components.products

    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.outlined.Check
    import androidx.compose.material3.Button
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import sasipca.models.Category
    import sasipca.models.ProductDetail
    import sasipca.models.ProductPut
    import sasipca.models.UnitType
    import sasipca.storage.ListsStore
    import sasipca.ui.components.LoadingWidget
    import sasipca.ui.components.ValidatedDropdown
    import sasipca.ui.components.ValidatedTextField
    import sasipca.ui.theme.CardTitle


    @Composable
    fun ProductEditForm(
        product: ProductDetail,
        isLoading: Boolean,
        errors: Map<String, String>,
        onSave: (ProductPut) -> Unit
    ) {
        // Inicialização de listas
        val categories: List<Category> = remember { ListsStore.categoriestypes.map { Category(it.id, it.type) } }
        val units: List<UnitType> = remember { ListsStore.unitTypes.map { UnitType(it.id, it.type) } }


        var editName by remember { mutableStateOf(product.name) }
        var editUnitSize by remember { mutableStateOf(product.unitSize.toString()) }
        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var selectedUnit by remember { mutableStateOf<UnitType?>(null) }

        LaunchedEffect(product) {
            product.let {
                editName = it.name
                editUnitSize = it.unitSize.toString()
                selectedCategory= categories.find { it.id == product.categoryId }
                selectedUnit = units.find { it.id == product.unitId }

            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Informações de Produto
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ){
                            CardTitle("Informações do Produto")

                            ValidatedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = "Nome",
                                error = errors["name"],
                                maxLength = 50,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ValidatedDropdown(
                                label = "Categoria",
                                items = categories,
                                selectedItem = selectedCategory,
                                onSelect = { selectedCategory = it },
                                error = errors["category"],
                                modifier = Modifier.fillMaxWidth()
                            )

                            ValidatedDropdown(
                                label = "Unidade",
                                items = units,
                                selectedItem = selectedUnit,
                                onSelect = { selectedUnit = it },
                                error = errors["unit"]
                            )

                            ValidatedTextField(
                                value = editUnitSize,
                                onValueChange = { editUnitSize = it },
                                label = "Quantidade",
                                error = errors["unitSize"],
                                maxLength = 11,
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }

                // Botão Guardar
                item {
                    Button(
                        onClick = {
                            val body = ProductPut(
                                name = editName,
                                unitSize = editUnitSize.toIntOrNull(),
                                unitId = selectedUnit?.id,
                                categoryId = selectedCategory?.id
                            )
                            onSave(body)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Alterações", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isLoading) {
               LoadingWidget()
            }
        }
    }