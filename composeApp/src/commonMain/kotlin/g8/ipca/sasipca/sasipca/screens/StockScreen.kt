package g8.ipca.sasipca.sasipca.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import g8.ipca.sasipca.sasipca.models.StockItemDTO
import g8.ipca.sasipca.sasipca.ui.components.HeaderSection
import g8.ipca.sasipca.sasipca.utils.getFormattedDatePt
import g8.ipca.sasipca.sasipca.viewmodels.StockViewModel

enum class ViewMode {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen() {
    val viewModel = remember { StockViewModel() }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    // Chamada inicial
    LaunchedEffect(Unit) { viewModel.loadStock() }

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

        HeaderSection("Inventário",getFormattedDatePt())

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
                onValueChange = { viewModel.loadStock(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                placeholder = {
                    Text("Pesquisar")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = Color(0xFF999999)
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
                    .background(Color.White)
            ) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filtrar",
                    tint = Color(0xFF3D4A7A)
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
                    .background(Color.White)
            ) {
                Icon(
                    if (viewMode == ViewMode.LIST) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                    contentDescription = "Alternar visualização",
                    tint = Color(0xFF3D4A7A)
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
                    StockItemCardGrid(
                        StockItemDTO(
                            barcode = item.barcode,
                            name = item.name,
                            category = item.category,
                            unit = item.unit,
                            unitSize = item.unitSize,
                            totalQuantity = item.totalQuantity,
                            reservedQuantity = item.reservedQuantity,
                            availableStock = item.availableStock
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StockItemCard(item: StockItemDTO) {
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
                    text = item.category,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "${item.availableStock} ${item.unit}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StockItemCardGrid(item: StockItemDTO) {
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
                    text = item.category,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "${item.availableStock} ${item.unit}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}