package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import sasipca.repositories.ProductRepository
import sasipca.storage.ListsStore
import sasipca.ui.components.BarcodeInputField
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.products.ProductsTable
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    productRepository: ProductRepository,
    onOpenProduct: (String) -> Unit,
    isReadOnly: Boolean = false // <--- NOVO PARÂMETRO
) {
    val productViewModel = remember { ProductViewModel(productRepository) }
    val focusManager = LocalFocusManager.current

    // Carregar Inicial
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }

    // Estados do ViewModel
    val filteredItems = productViewModel.filteredItems
    val isLoading = productViewModel.isLoading
    val searchQuery = productViewModel.searchQuery
    val currentPage = productViewModel.currentPage
    val totalPages = productViewModel.totalPages
    val selectedCategoryId = productViewModel.selectedCategoryId

    // Estado Local para Menu
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        // SÓ MOSTRA O FAB SE NÃO FOR MODO DE LEITURA (Admin/Staff)
        floatingActionButton = {
            if (!isReadOnly) {
                FloatingActionButton(
                    onClick = {
                        // Lógica para criar novo produto (ex: navigator.push(CreateProductScreen))
                        onOpenProduct("new")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Produto")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues) // Padding do Scaffold
        ) {

            Header("Inventário", getFormattedDatePt())

            // --- BARRA SUPERIOR (PESQUISA + FILTRO) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                BarcodeInputField(
                    value = searchQuery,
                    onValueChange = { productViewModel.loadProducts(search = it) },
                    label = "Pesquisar",
                    placeholder = "Nome ou Scan...",
                    suggestions = emptyList(),
                    onSuggestionSelected = {},
                    modifier = Modifier.weight(1f)
                )

                // --- BOTÃO DE FILTRO COM DROPDOWN ---
                Box {
                    IconButton(
                        onClick = { showFilterMenu = true },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedCategoryId != null) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = "Filtrar por Categoria",
                            tint = if (selectedCategoryId != null) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas as Categorias") },
                            onClick = {
                                productViewModel.onCategoryChange(null)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (selectedCategoryId == null) Icon(Icons.Default.Check, null)
                            }
                        )

                        Divider()

                        ListsStore.categoriestypes.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.type) },
                                onClick = {
                                    productViewModel.onCategoryChange(category.id)
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    if (selectedCategoryId == category.id) Icon(Icons.Default.Check, null)
                                }
                            )
                        }
                    }
                }
            }

            // --- TABELA RESPONSIVA ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                if (isLoading) {
                    LoadingWidget()
                } else {
                    ProductsTable(
                        products = filteredItems,
                        isLoading = false,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onNextPage = { productViewModel.goToNextPage() },
                        onPreviousPage = { productViewModel.goToPreviousPage() },
                        onProductClick = onOpenProduct
                    )
                }
            }
        }
    }
}