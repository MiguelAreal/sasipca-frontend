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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.BeneficiaryListDTO
import sasipca.repositories.BeneficiaryRepository
import sasipca.ui.components.Header
import sasipca.viewmodels.BeneficiariesViewModel

enum class BeneficiaryViewMode {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariesScreen(
    beneficiaryRepository: BeneficiaryRepository,
    onAddBeneficiary: () -> Unit = {},
    onOpenBeneficiary: (Int) -> Unit = {}
) {
    val viewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }

    var viewMode by remember { mutableStateOf(BeneficiaryViewMode.LIST) }
    var showFilters by remember { mutableStateOf(false) }

    val isLoading by remember { viewModel::isLoading }
    val errorMessage by remember { viewModel::errorMessage }
    val beneficiaries by remember { viewModel::beneficiaries }
    val searchTerm by remember { viewModel::searchTerm }
    val currentPage by remember { viewModel::currentPage }
    val orderBy by remember { viewModel::orderBy }

    val snackbarHostState = remember { SnackbarHostState() }

    // Carregar dados iniciais
    LaunchedEffect(Unit) {
        viewModel.loadBeneficiaries()
    }

    // Mostrar erros
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBeneficiary,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar beneficiário")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Header("Beneficiários")

            // Barra de pesquisa + botões
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { viewModel.loadBeneficiaries(search = it, page = 1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    placeholder = { Text("Pesquisar beneficiário...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Filtro (abre/fecha painel)
                IconButton(
                    onClick = { showFilters = !showFilters },
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

                // Alternar visualização (Lista / Grelha)
                IconButton(
                    onClick = {
                        viewMode = if (viewMode == BeneficiaryViewMode.LIST)
                            BeneficiaryViewMode.GRID else BeneficiaryViewMode.LIST
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        if (viewMode == BeneficiaryViewMode.LIST)
                            Icons.Outlined.GridView
                        else
                            Icons.Outlined.ViewList,
                        contentDescription = "Alternar visualização"
                    )
                }
            }

            // Painel de filtros
            if (showFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = orderBy == "asc",
                        onClick = { viewModel.loadBeneficiaries(order = "asc") },
                        label = { Text("A-Z") },
                        leadingIcon = {
                            Icon(Icons.Outlined.ArrowUpward, contentDescription = null)
                        }
                    )
                    FilterChip(
                        selected = orderBy == "desc",
                        onClick = { viewModel.loadBeneficiaries(order = "desc") },
                        label = { Text("Z-A") },
                        leadingIcon = {
                            Icon(Icons.Outlined.ArrowDownward, contentDescription = null)
                        }
                    )
                }
            }

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                beneficiaries == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sem dados disponíveis")
                    }
                }

                beneficiaries!!.data.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum beneficiário encontrado")
                    }
                }

                else -> {
                    val list = beneficiaries!!.data

                    if (viewMode == BeneficiaryViewMode.LIST) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(list) { beneficiary ->
                                BeneficiaryListItemCard(
                                    beneficiary = beneficiary,
                                    onClick = { onOpenBeneficiary(beneficiary.beneficiaryId) }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 200.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(list) { beneficiary ->
                                BeneficiaryGridItemCard(
                                    beneficiary = beneficiary,
                                    onClick = { onOpenBeneficiary(beneficiary.beneficiaryId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeneficiaryListItemCard(
    beneficiary: BeneficiaryListDTO,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = beneficiary.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = beneficiary.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                beneficiary.email?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun BeneficiaryGridItemCard(
    beneficiary: BeneficiaryListDTO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(1.dp),
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
                    text = beneficiary.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                beneficiary.email?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = beneficiary.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
