package sasipca.ui.components.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.Product
import sasipca.storage.ListsStore
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.LoadingWidget
import sasipca.ui.theme.CardTitle


enum class ProductSortColumn {
    NAME, CATEGORY, TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_STOCK
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

@Composable
fun ProductsTable(
    products: List<Product>,
    isLoading: Boolean,
    currentPage: Int = 1,
    totalPages: Int = 1,
    onNextPage: () -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onProductClick: (String) -> Unit = {}
) {
    var sortColumn by remember { mutableStateOf(ProductSortColumn.NAME) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASCENDING) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val sortedProducts = remember(products, sortColumn, sortDirection) {
        val sorted = when (sortColumn) {
            ProductSortColumn.NAME -> products.sortedBy { it.name }
            ProductSortColumn.CATEGORY -> products.sortedBy { ListsStore.getCategoryName(it.categoryId) }
            ProductSortColumn.TOTAL_QUANTITY -> products.sortedBy { it.totalQuantity ?: 0 }
            ProductSortColumn.RESERVED_QUANTITY -> products.sortedBy { it.reservedQuantity ?: 0 }
            ProductSortColumn.AVAILABLE_STOCK -> products.sortedBy { it.availableStock ?: 0 }
        }
        if (sortDirection == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                if (sortedProducts.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sem produtos registados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Table Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProductsTableHeader(
                                    text = "Produto",
                                    sortColumn = ProductSortColumn.NAME,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.25f),
                                    onClick = {
                                        if (sortColumn == ProductSortColumn.NAME)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = ProductSortColumn.NAME
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                                ProductsTableHeader(
                                    text = "Categoria",
                                    sortColumn = ProductSortColumn.CATEGORY,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.2f),
                                    onClick = {
                                        if (sortColumn == ProductSortColumn.CATEGORY)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = ProductSortColumn.CATEGORY
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                                ProductsTableHeader(
                                    text = "Qtd. Total",
                                    sortColumn = ProductSortColumn.TOTAL_QUANTITY,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.18f),
                                    onClick = {
                                        if (sortColumn == ProductSortColumn.TOTAL_QUANTITY)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = ProductSortColumn.TOTAL_QUANTITY
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                                ProductsTableHeader(
                                    text = "Qtd. Reservada",
                                    sortColumn = ProductSortColumn.RESERVED_QUANTITY,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.18f),
                                    onClick = {
                                        if (sortColumn == ProductSortColumn.RESERVED_QUANTITY)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = ProductSortColumn.RESERVED_QUANTITY
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                                ProductsTableHeader(
                                    text = "Qtd. Disponível",
                                    sortColumn = ProductSortColumn.AVAILABLE_STOCK,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    modifier = Modifier.weight(0.19f),
                                    onClick = {
                                        if (sortColumn == ProductSortColumn.AVAILABLE_STOCK)
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                        else {
                                            sortColumn = ProductSortColumn.AVAILABLE_STOCK
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                            }
                        }

                        items(sortedProducts) { product ->
                            ProductRow(
                                product = product,
                                isSelected = selectedProduct?.barcode == product.barcode,
                                onClick = {
                                    onProductClick(product.barcode)
                                }
                            )
                        }
                    }
                }

                if (isLoading) {
                    LoadingWidget()
                }
            }

            // Pagination Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousPage,
                    enabled = currentPage > 1
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Página anterior"
                    )
                }

                Text(
                    text = "Página $currentPage de $totalPages",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onNextPage,
                    enabled = currentPage < totalPages
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Próxima página"
                    )
                }
            }
        }
    }
}