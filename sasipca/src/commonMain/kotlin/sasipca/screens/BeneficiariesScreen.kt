package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sasipca.repositories.BeneficiaryRepository
import sasipca.ui.components.beneficiaries.CreateBeneficiaryPopup
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.SearchInputField // <--- Novo Componente
import sasipca.ui.components.beneficiaries.BeneficiaryListItemCard
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.BeneficiariesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariesScreen(
    beneficiaryRepository: BeneficiaryRepository,
    onOpenBeneficiary: (Int) -> Unit
) {
    val viewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }

    // Estados do ViewModel
    val isLoading = viewModel.isLoading
    val beneficiaries = viewModel.beneficiaries
    val orderBy = viewModel.orderBy

    // Estados Locais
    var showFilters by remember { mutableStateOf(false) }
    var searchTerm by remember { mutableStateOf("") }
    var showCreatePopup by remember { mutableStateOf(false) }

    // Debounce de pesquisa (Lógica atualizada e robusta)
    LaunchedEffect(searchTerm) {
        if (searchTerm.isNotEmpty()) {
            delay(500)
        }
        viewModel.loadBeneficiaries(search = searchTerm, page = 1)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {
            Header("Beneficiários", getFormattedDatePt())

            // --- BARRA DE PESQUISA E FILTROS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Componente Reutilizável com botão "Limpar"
                SearchInputField(
                    query = searchTerm,
                    onQueryChange = { searchTerm = it },
                    placeholder = "Nome ou e-mail",
                    modifier = Modifier.weight(1f)
                )

                // Botão de Filtros
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .size(56.dp) // Alinhado com a altura do input (60dp visualmente aproximado com padding interno)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = "Filtrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- PAINEL DE FILTROS ---
            if (showFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = orderBy == "asc",
                        onClick = { viewModel.loadBeneficiaries(order = "asc") },
                        label = { Text("A-Z") },
                        leadingIcon = { Icon(Icons.Outlined.ArrowUpward, null) }
                    )
                    FilterChip(
                        selected = orderBy == "desc",
                        onClick = { viewModel.loadBeneficiaries(order = "desc") },
                        label = { Text("Z-A") },
                        leadingIcon = { Icon(Icons.Outlined.ArrowDownward, null) }
                    )
                }
            }

            // --- LISTA DE CONTEÚDO ---
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && beneficiaries == null -> { // Loading inicial
                        LoadingWidget()
                    }
                    beneficiaries?.data.isNullOrEmpty() && !isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum beneficiário encontrado", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                    else -> {
                        val list = beneficiaries?.data ?: emptyList()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp), // Padding bottom para o FAB
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list, key = { it.beneficiaryId }) { beneficiary ->
                                BeneficiaryListItemCard(
                                    beneficiary = beneficiary,
                                    onClick = { onOpenBeneficiary(beneficiary.beneficiaryId) }
                                )
                            }
                        }
                    }
                }

                // Loading Overlay se estiver a paginar ou filtrar mas já tiver dados
                if (isLoading && beneficiaries != null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }
            }
        }

        // --- FAB (FLOATING ACTION BUTTON) ---
        FloatingActionButton(
            onClick = { showCreatePopup = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.onPrimary)
        }

        // --- POPUP DE CRIAÇÃO ---
        if (showCreatePopup) {
            CreateBeneficiaryPopup(
                repository = beneficiaryRepository,
                onDismiss = { showCreatePopup = false },
                onCreated = { viewModel.loadBeneficiaries() }
            )
        }
    }
}