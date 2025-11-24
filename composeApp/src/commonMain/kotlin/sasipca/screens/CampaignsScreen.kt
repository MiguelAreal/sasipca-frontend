package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.Campaign
import sasipca.repositories.CampaignRepository
import sasipca.repositories.ListsRepository
import sasipca.ui.components.Header
import sasipca.ui.components.campaigns.CampaignEditDialog
import sasipca.ui.components.campaigns.CampaignsGrid
import sasipca.utils.getFormattedDatePt
import sasipca.viewmodels.CampaignViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    campaignRepository: CampaignRepository,
    listsRepository: ListsRepository
) {
    val campaignViewModel = remember {
        CampaignViewModel(campaignRepository, listsRepository)
    }

    // Estados
    val filteredItems by remember { campaignViewModel::filteredItems }
    val isLoading by remember { campaignViewModel::isLoading }
    val searchQuery by remember { campaignViewModel::searchQuery }
    val currentPage by remember { campaignViewModel::currentPage }
    val totalPages by remember { campaignViewModel::totalPages }

    // Estado do Dialog vindo do ViewModel
    val isDialogOpen by remember { campaignViewModel::isDialogOpen }
    val formState by remember { campaignViewModel::formState }

    LaunchedEffect(Unit) { campaignViewModel.loadCampaigns() }

    // Estado local para Dialog
    var selectedCampaign by remember { mutableStateOf<Campaign?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Define o fundo do ecrã conforme o tema (geralmente branco em Light Mode)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Header("Campanhas", getFormattedDatePt())

        // --- Barra de Pesquisa e Botões ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { campaignViewModel.loadCampaigns(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(65.dp),
                placeholder = { Text("Pesquisar campanha...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Botão Filtro
            IconButton(
                onClick = { /* TODO: Implementar Filtros Avançados */ },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface) // Fundo branco/surface no botão
            ) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filtrar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Botão Nova Campanha
            FloatingActionButton(
                onClick = { campaignViewModel.startNewCampaign() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(50.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Nova")
            }
        }

        // --- Conteúdo Principal (Grid) ---
        // Box com weight para ocupar o espaço e padding igual à ProductsScreen
        Box(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
            CampaignsGrid(
                campaigns = filteredItems,
                isLoading = isLoading,
                onCampaignClick = { campaign ->
                    // Abre edição imediatamente
                    campaignViewModel.selectCampaignToEdit(campaign)
                }
            )
        }

        // --- Rodapé de Paginação ---
        if (totalPages > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(MaterialTheme.colorScheme.background), // Garante fundo correto no rodapé
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { campaignViewModel.goToPreviousPage() },
                    enabled = currentPage > 1 && !isLoading
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Anterior",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Página $currentPage de $totalPages",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { campaignViewModel.goToNextPage() },
                    enabled = currentPage < totalPages && !isLoading
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Seguinte",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    // --- Dialog de Detalhes ---
    if (isDialogOpen) {
        CampaignEditDialog(
            formState = formState,
            onDismiss = { campaignViewModel.closeDialog() },
            viewModel = campaignViewModel
        )
    }
}