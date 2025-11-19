package sasipca.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.repositories.BeneficiaryRepository
import sasipca.ui.components.beneficiaries.CreateBeneficiaryPopup
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.beneficiaries.*
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.BeneficiariesViewModel

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariesScreen(
    beneficiaryRepository: BeneficiaryRepository,
    onAddBeneficiary: () -> Unit = {},
    onOpenBeneficiary: (Int) -> Unit = {}
) {
    val viewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }
    var showFilters by remember { mutableStateOf(false) }

    val isLoading by remember { viewModel::isLoading }
    val beneficiaries by remember { viewModel::beneficiaries }
    val currentPage by remember { viewModel::currentPage }
    var searchTerm by remember { mutableStateOf("") } // texto local da pesquisa
    val orderBy by remember { viewModel::orderBy }

    var showCreatePopup by remember { mutableStateOf(false) }

    // Debounce de 500ms: só pesquisa depois de parar de digitar
    LaunchedEffect(searchTerm) {
        kotlinx.coroutines.delay(500) // intervalo entre digitação e pesquisa
        viewModel.loadBeneficiaries(search = searchTerm, page = 1)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePopup = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar beneficiário")
            }
            if (showCreatePopup) {
                CreateBeneficiaryPopup(
                    repository = beneficiaryRepository,
                    onDismiss = { showCreatePopup = false },
                    onCreated = { viewModel.loadBeneficiaries() } // recarrega a lista após criar
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Header("Beneficiários",getFormattedDatePt())

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
                    onValueChange = { searchTerm = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(65.dp),
                    placeholder = { Text("Pesquisar beneficiário...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
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
                        LoadingWidget()
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
                }
            }
        }
    }
}
