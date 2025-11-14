package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.ProductItemUI
import sasipca.repositories.ProductRepository
import sasipca.ui.components.Header
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.ProductViewModel

enum class ViewMode {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(productRepository: ProductRepository) {
    val viewModel = remember { ProductViewModel(productRepository) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    // Chamada inicial
    LaunchedEffect(Unit) { viewModel.loadProducts() }

    val filteredItems by remember { viewModel::filteredItems }
    val isLoading by remember { viewModel::isLoading }
    val errorMessage by remember { viewModel::errorMessage }
    val searchQuery by remember { viewModel::searchQuery }
    val currentPage by remember { viewModel::currentPage }
    val totalPages by remember { viewModel::totalPages }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Header("Inventário", getFormattedDatePt())

        // Barra de pesquisa e botões
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
                onValueChange = { viewModel.loadProducts(it) },
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

            // Botão de alternar visualização
            IconButton(
                onClick = {
                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    if (viewMode == ViewMode.LIST) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                    contentDescription = "Alternar visualização"
                )
            }
        }

        // Lista ou grelha de itens
        if (viewMode == ViewMode.LIST) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredItems) { item ->
                    StockItemCard(item)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredItems) { item ->
                    ProductItemCardGrid(item)

                }
            }
        }
    }
}

@Composable
fun StockItemCard(item: ProductItemUI) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Ação ao clicar */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.categoryName, // antes era categoryId.toString()
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "${item.availableStock} ${item.unitName}", // antes era unitId
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProductItemCardGrid(item: ProductItemUI) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { /* TODO: Ação ao clicar */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Text(
                    text = item.categoryName, // antes categoryId.toString()
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "${item.availableStock} ${item.unitName}", // antes unitId
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}