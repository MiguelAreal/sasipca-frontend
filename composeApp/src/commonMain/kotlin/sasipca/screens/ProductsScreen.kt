package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import sasipca.repositories.ProductRepository
import sasipca.ui.components.Header
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.ProductViewModel
import androidx.compose.material.icons.Icons
import sasipca.ui.components.products.ProductsTable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    productRepository: ProductRepository,
    onOpenProduct: (String) -> Unit = {}
) {
    val productviewModel = remember { ProductViewModel(productRepository) }

    // Chamada inicial
    LaunchedEffect(Unit) { productviewModel.loadProducts() }

    val filteredItems by remember { productviewModel::filteredItems }
    val isLoading by remember { productviewModel::isLoading }
    val searchQuery by remember { productviewModel::searchQuery }
    val currentPage by remember { productviewModel::currentPage }
    val totalPages by remember { productviewModel::totalPages }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Header("Inventário", getFormattedDatePt())

        // Barra de pesquisa e botão de filtro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { productviewModel.loadProducts(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(65.dp),
                placeholder = {
                    Text("Pesquisar produto...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Pesquisar"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Botão de filtro
            IconButton(
                onClick = { /* TODO: Implementar filtro */ },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filtrar"
                )
            }
        }

        // Tabela de produtos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ProductsTable(
                products = filteredItems,
                isLoading = isLoading,
                // 1. Passar estados de Paginação
                currentPage = currentPage,
                totalPages = totalPages,
                // 2. Passar funções de Paginação
                onNextPage = { productviewModel.goToNextPage() },
                onPreviousPage = { productviewModel.goToPreviousPage() },
                onProductClick = onOpenProduct
            )
        }
    }
}