package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import g8.ipca.sasipca.sasipca.ui.components.Header

// Data classes (adicionar ao teu projeto)
data class BeneficiaryListItem(
    val id: Int,
    val name: String,
    val email: String?
)

data class PaginatedBeneficiaries(
    val data: List<BeneficiaryListItem>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariesScreen() {
    var searchText by remember { mutableStateOf("") }
    var orderBy by remember { mutableStateOf("asc") }
    var currentPage by remember { mutableStateOf(1) }
    var showFilters by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Mock data - substituir por chamada à API
    val mockBeneficiaries = remember {
        PaginatedBeneficiaries(
            data = listOf(
                BeneficiaryListItem(1, "Ana Sofia Costa", "ana.costa@ipca.pt"),
                BeneficiaryListItem(2, "Bruno Miguel Santos", "bruno.santos@ipca.pt"),
                BeneficiaryListItem(3, "Carla Fernandes", "carla.fernandes@ipca.pt"),
                BeneficiaryListItem(4, "Daniel Oliveira", "daniel.oliveira@ipca.pt"),
                BeneficiaryListItem(5, "Eva Maria Silva", "eva.silva@ipca.pt"),
                BeneficiaryListItem(6, "Fernando Pereira", null),
                BeneficiaryListItem(7, "Gabriela Rodrigues", "gabriela.rodrigues@ipca.pt"),
                BeneficiaryListItem(8, "Hugo Almeida", "hugo.almeida@ipca.pt")
            ),
            pageNumber = 1,
            pageSize = 10,
            totalCount = 8,
            totalPages = 1
        )
    }


    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Header("Lista de Beneficiários")

                    // Barra de pesquisa
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            currentPage = 1
                            // TODO: Chamar API com novo searchText
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        placeholder = { Text("Pesquisar por nome...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Pesquisar"
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchText = ""
                                    currentPage = 1
                                    // TODO: Chamar API sem filtro
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpar"
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters)
                                Icons.Filled.FilterAlt
                            else
                                Icons.Outlined.FilterAlt,
                            contentDescription = "Filtros",
                            tint = if (showFilters)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Filtros expansíveis
                    if (showFilters) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Ordenação",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = orderBy == "asc",
                                        onClick = {
                                            orderBy = "asc"
                                            currentPage = 1
                                            // TODO: Chamar API com orderBy = "asc"
                                        },
                                        label = { Text("A-Z") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                    FilterChip(
                                        selected = orderBy == "desc",
                                        onClick = {
                                            orderBy = "desc"
                                            currentPage = 1
                                            // TODO: Chamar API com orderBy = "desc"
                                        },
                                        label = { Text("Z-A") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = TODO(),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Adicionar beneficiário",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (mockBeneficiaries.data.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "Nenhum beneficiário encontrado",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (searchText.isNotEmpty()) {
                            TextButton(onClick = {
                                searchText = ""
                                currentPage = 1
                            }) {
                                Text("Limpar filtros")
                            }
                        }
                    }
                }
            } else {
                // Lista de beneficiários
                Column(modifier = Modifier.fillMaxSize()) {
                    // Contador de resultados
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${mockBeneficiaries.totalCount} beneficiários",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Página ${mockBeneficiaries.pageNumber} de ${mockBeneficiaries.totalPages}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mockBeneficiaries.data) { beneficiary ->
                            BeneficiaryListItemCard(
                                beneficiary = beneficiary,
                                onClick = { TODO() }
                            )
                        }
                    }

                    // Paginação
                    if (mockBeneficiaries.totalPages > 1) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentPage > 1) {
                                            currentPage--
                                            // TODO: Chamar API com nova página
                                        }
                                    },
                                    enabled = currentPage > 1
                                ) {
                                    Icon(
                                        Icons.Default.ChevronLeft,
                                        contentDescription = "Página anterior"
                                    )
                                }

                                Text(
                                    text = "Página $currentPage de ${mockBeneficiaries.totalPages}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                IconButton(
                                    onClick = {
                                        if (currentPage < mockBeneficiaries.totalPages) {
                                            currentPage++
                                            // TODO: Chamar API com nova página
                                        }
                                    },
                                    enabled = currentPage < mockBeneficiaries.totalPages
                                ) {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Próxima página"
                                    )
                                }
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
    beneficiary: BeneficiaryListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar com inicial
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

            // Informações
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = beneficiary.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (beneficiary.email != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = beneficiary.email,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}