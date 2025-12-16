package sasipca.ui.components.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.Product
import sasipca.storage.ListsStore
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.SessionManager
import sasipca.ui.components.LoadingWidget

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
    // 1. Determinar se é Beneficiário (Read-only)
    val userRole = remember { SessionManager.getUserRole() }
    val isBeneficiary = userRole == "Beneficiary"

    // Estado de Ordenação Local
    var sortColumn by remember { mutableStateOf(ProductSortColumn.NAME) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASCENDING) }

    // Lógica de Ordenação
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

    Column(modifier = Modifier.fillMaxSize()) {

        // --- CONTEÚDO (TABELA OU LISTA) ---
        Box(modifier = Modifier.weight(1f)) {
            if (sortedProducts.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem produtos registados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // DECISÃO DE LAYOUT RESPONSIVO
                if (isLargeScreen()) {
                    DesktopTableView(
                        products = sortedProducts,
                        sortColumn = sortColumn,
                        sortDirection = sortDirection,
                        onSortChange = { col ->
                            if (sortColumn == col) {
                                sortDirection = if (sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                            } else {
                                sortColumn = col
                                sortDirection = SortDirection.ASCENDING
                            }
                        },
                        onProductClick = onProductClick,
                        isBeneficiary = isBeneficiary // Passar flag
                    )
                } else {
                    MobileCardListView(
                        products = sortedProducts,
                        onProductClick = onProductClick,
                        isBeneficiary = isBeneficiary // Passar flag
                    )
                }
            }

            if (isLoading) {
                LoadingWidget()
            }
        }

        // --- RODAPÉ DE PAGINAÇÃO (COMUM) ---
        if (totalPages > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousPage, enabled = currentPage > 1) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Anterior")
                }
                Text(
                    text = "Página $currentPage de $totalPages",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onNextPage, enabled = currentPage < totalPages) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Seguinte")
                }
            }
        }
    }
}

// ==========================================
// LAYOUT DESKTOP (TABELA CLÁSSICA)
// ==========================================
@Composable
private fun DesktopTableView(
    products: List<Product>,
    sortColumn: ProductSortColumn,
    sortDirection: SortDirection,
    onSortChange: (ProductSortColumn) -> Unit,
    onProductClick: (String) -> Unit,
    isBeneficiary: Boolean // Novo param
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Colunas Comuns
                ProductsTableHeader("Produto", ProductSortColumn.NAME, sortColumn, sortDirection, Modifier.weight(0.4f)) { onSortChange(ProductSortColumn.NAME) }
                ProductsTableHeader("Categoria", ProductSortColumn.CATEGORY, sortColumn, sortDirection, Modifier.weight(0.3f)) { onSortChange(ProductSortColumn.CATEGORY) }

                // Colunas Exclusivas Admin
                if (!isBeneficiary) {
                    ProductsTableHeader("Total", ProductSortColumn.TOTAL_QUANTITY, sortColumn, sortDirection, Modifier.weight(0.15f)) { onSortChange(ProductSortColumn.TOTAL_QUANTITY) }
                    ProductsTableHeader("Reservado", ProductSortColumn.RESERVED_QUANTITY, sortColumn, sortDirection, Modifier.weight(0.15f)) { onSortChange(ProductSortColumn.RESERVED_QUANTITY) }
                }

                // Coluna Disponível (Todos vêm)
                ProductsTableHeader("Disponível", ProductSortColumn.AVAILABLE_STOCK, sortColumn, sortDirection, Modifier.weight(0.2f)) { onSortChange(ProductSortColumn.AVAILABLE_STOCK) }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Linhas
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(products) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(product.barcode) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, Modifier.weight(0.4f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(ListsStore.getCategoryName(product.categoryId), Modifier.weight(0.3f), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (!isBeneficiary) {
                            Text("${product.totalQuantity ?: 0}", Modifier.weight(0.15f), fontSize = 14.sp)
                            Text("${product.reservedQuantity ?: 0}", Modifier.weight(0.15f), fontSize = 14.sp)
                        }

                        // Destaque visual para stock disponível
                        val avail = product.availableStock ?: 0
                        Text(
                            text = "$avail",
                            Modifier.weight(0.2f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(avail > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// ==========================================
// LAYOUT MOBILE (LISTA DE CARTÕES)
// ==========================================
@Composable
private fun MobileCardListView(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    isBeneficiary: Boolean // Novo param
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick(product.barcode) },
                elevation = CardDefaults.cardElevation(1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Linha 1: Nome e Categoria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = ListsStore.getCategoryName(product.categoryId),
                                modifier = Modifier.padding(4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Linha 2: Estatísticas em Grid
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (!isBeneficiary) {
                            MobileStatItem("Total", "${product.totalQuantity ?: 0}", Modifier.weight(1f))
                            MobileStatItem("Reservado", "${product.reservedQuantity ?: 0}", Modifier.weight(1f))
                        }

                        MobileStatItem(
                            "Disponível",
                            "${product.availableStock ?: 0}",
                            Modifier.weight(1f), // Se for beneficiary, ocupa tudo
                            isHighlight = true,
                            isPositive = (product.availableStock ?: 0) > 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileStatItem(label: String, value: String, modifier: Modifier = Modifier, isHighlight: Boolean = false, isPositive: Boolean = true) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if(isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if(isHighlight) (if(isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) else MaterialTheme.colorScheme.onSurface
        )
    }
}
