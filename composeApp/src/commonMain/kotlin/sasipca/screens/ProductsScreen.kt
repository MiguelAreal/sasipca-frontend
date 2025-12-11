package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import sasipca.repositories.ProductRepository
import sasipca.ui.components.BarcodeInputField // <--- Usamos o componente com Scanner
import sasipca.ui.components.Header
import sasipca.ui.components.products.ProductsTable
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    productRepository: ProductRepository,
    onOpenProduct: (String) -> Unit // Removemos o valor por defeito para obrigar a passar a navegação
) {
    val productViewModel = remember { ProductViewModel(productRepository) }
    val focusManager = LocalFocusManager.current

    // Chamada inicial
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }

    // Acesso direto aos estados (visto que o VM usa mutableStateOf)
    val filteredItems = productViewModel.filteredItems
    val isLoading = productViewModel.isLoading
    val searchQuery = productViewModel.searchQuery
    val currentPage = productViewModel.currentPage
    val totalPages = productViewModel.totalPages

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

            // --- BARRA DE PESQUISA COM SCANNER ---
            // Usamos o BarcodeInputField mas sem sugestões (lista vazia),
            // apenas para aproveitar o design e o botão de scan.
            BarcodeInputField(
                value = searchQuery,
                onValueChange = {
                    productViewModel.loadProducts(it)
                },
                label = "Pesquisar",
                placeholder = "Nome ou Scan...",
                suggestions = emptyList(), // Não queremos dropdown aqui, é filtro de lista
                onSuggestionSelected = {},
                modifier = Modifier.weight(1f)
            )

            // Botão de filtro
            IconButton(
                onClick = { /* TODO: Implementar filtro avançado */ },
                modifier = Modifier
                    .size(56.dp) // Altura padrão do TextField para alinhar
                    .clip(RoundedCornerShape(8.dp)) // Quadrado arredondado igual ao input
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filtrar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tabela de produtos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 0.dp) // Ajustei padding vertical
        ) {
            ProductsTable(
                products = filteredItems,
                isLoading = isLoading,
                // Passar estados de Paginação
                currentPage = currentPage,
                totalPages = totalPages,
                // Passar funções de Paginação
                onNextPage = { productViewModel.goToNextPage() },
                onPreviousPage = { productViewModel.goToPreviousPage() },
                onProductClick = onOpenProduct
            )
        }
    }
}