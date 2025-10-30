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
import g8.ipca.sasipca.sasipca.ui.components.HeaderSection
import g8.ipca.sasipca.sasipca.ui.utils.getFormattedDatePt
import java.text.SimpleDateFormat
import java.util.*

data class StockItem(
    val name: String,
    val category: String,
    val quantity: String,
    val unit: String
)

enum class ViewMode {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    val stockItems = listOf(
        StockItem("Arroz Cigala", "Alimentar", "16", "Kg"),
        StockItem("Gel de banho Continente", "Higiene", "60", "L"),
        StockItem("Atum Enlatado", "Alimentar", "8", "Uni"),
        StockItem("Azeite", "Alimentar", "8", "L"),
        StockItem("Esponjas de Loiça", "Higiene Doméstica", "6", "Uni"),
        StockItem("Pasta de dentes", "Higiene", "4", "Uni"),
        StockItem("Sabonete Líquido", "Higiene", "12", "L"),
        StockItem("Detergente Roupa", "Higiene Doméstica", "5", "L"),
        StockItem("Papel Higiénico", "Higiene", "24", "Uni"),
        StockItem("Massa Esparguete", "Alimentar", "10", "Kg")
    )

    val filteredItems = stockItems.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

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
                onValueChange = { searchQuery = it },
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
                    StockItemCardGrid(item)
                }
            }
        }
    }
}

@Composable
fun StockItemCard(item: StockItem) {
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
                text = "${item.quantity} ${item.unit}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StockItemCardGrid(item: StockItem) {
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
                text = "${item.quantity} ${item.unit}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}