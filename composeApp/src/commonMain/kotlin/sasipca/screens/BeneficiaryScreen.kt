package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sasipca.models.BeneficiaryGetDTO
import sasipca.models.BeneficiaryPostDTO
import sasipca.models.VDeliveryDTO
import sasipca.navigation.NavigationService
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.StockRepository
import sasipca.ui.components.Header
import sasipca.viewmodels.BeneficiaryDetailViewModel
import sasipca.viewmodels.DeliveriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryScreen(
    beneficiaryId: Int,
    repository: BeneficiaryRepository,
    stockRepository: StockRepository
) {
    val viewModel = remember { BeneficiaryDetailViewModel(repository) }
    val deliveriesViewModel = remember { DeliveriesViewModel(stockRepository) }
    val scope = rememberCoroutineScope()

    val beneficiary = viewModel.getBeneficiary
    val isLoading by remember { viewModel::isLoading }
    val deliveries by deliveriesViewModel.deliveries.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Carrega dados iniciais
    LaunchedEffect(beneficiaryId) {
        viewModel.loadBeneficiary(beneficiaryId)
        deliveriesViewModel.loadBeneficiaryDeliveries(beneficiaryId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            title = "Beneficiário",
            subTitle = beneficiary?.name ?: ""
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLargeScreen = maxWidth >= 900.dp

            if (isLargeScreen) {
                // Layout lado a lado para ecrãs grandes
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Coluna da esquerda - Edição
                    Box(modifier = Modifier.weight(1f)) {
                        BeneficiaryEditForm(
                            beneficiary = beneficiary,
                            isLoading = isLoading,
                            onSave = { dto ->
                                scope.launch {
                                    viewModel.updateBeneficiary(beneficiaryId, dto) {
                                        NavigationService.goBack()
                                    }
                                }
                            }
                        )
                    }

                    // Coluna da direita - Histórico
                    Box(modifier = Modifier.weight(1f)) {
                        BeneficiaryDeliveriesHist(
                            deliveries = deliveries,
                            isLoading = deliveriesViewModel.isLoading.collectAsState().value
                        )
                    }
                }
            } else {
                // Layout com separadores para ecrãs pequenos
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Dados") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Histórico") }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTab) {
                            0 -> BeneficiaryEditForm(
                                beneficiary = beneficiary,
                                isLoading = isLoading,
                                onSave = { dto ->
                                    scope.launch {
                                        viewModel.updateBeneficiary(beneficiaryId, dto) {
                                            NavigationService.goBack()
                                        }
                                    }
                                }
                            )
                            1 -> BeneficiaryDeliveriesHist(
                                deliveries = deliveries,
                                isLoading = deliveriesViewModel.isLoading.collectAsState().value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeneficiaryEditForm(
    beneficiary: BeneficiaryGetDTO?,
    isLoading: Boolean,
    onSave: (BeneficiaryPostDTO) -> Unit
) {
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editContact by remember { mutableStateOf("") }
    var editNif by remember { mutableStateOf("") }
    var editStreet by remember { mutableStateOf("") }
    var editNumber by remember { mutableStateOf("") }
    var editPostalcode by remember { mutableStateOf("") }
    var editStudentnum by remember { mutableStateOf("") }
    var editCourse by remember { mutableStateOf("") }
    var editCurricularYear by remember { mutableStateOf("") }
    var editGlobalObs by remember { mutableStateOf("") }
    var editParticularObs by remember { mutableStateOf("") }

    LaunchedEffect(beneficiary) {
        beneficiary?.let {
            editName = it.name
            editEmail = it.email
            editContact = it.contact
            editNif = it.nif?.toString() ?: ""
            editStreet = it.street ?: ""
            editNumber = it.number?.toString() ?: ""
            editPostalcode = it.postalCode ?: ""
            editStudentnum = it.studentNum?.toString() ?: ""
            editCourse = it.course ?: ""
            editCurricularYear = it.curricularYear?.toString() ?: ""
            editGlobalObs = it.globalObs ?: ""
            editParticularObs = it.particularObs ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Informações Pessoais
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Informações Pessoais",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nome") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editContact,
                            onValueChange = { editContact = it },
                            label = { Text("Contacto") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editNif,
                            onValueChange = { editNif = it },
                            label = { Text("NIF") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Morada
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Morada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = editStreet,
                            onValueChange = { editStreet = it },
                            label = { Text("Rua") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editNumber,
                            onValueChange = { editNumber = it },
                            label = { Text("Número da Porta") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editPostalcode,
                            onValueChange = { editPostalcode = it },
                            label = { Text("Código-Postal") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Informações Académicas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Informações Académicas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = editStudentnum,
                            onValueChange = { editStudentnum = it },
                            label = { Text("Número de estudante") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editCourse,
                            onValueChange = { editCourse = it },
                            label = { Text("Curso") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editCurricularYear,
                            onValueChange = { editCurricularYear = it },
                            label = { Text("Ano Curricular") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Observações
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Observações",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = editGlobalObs,
                            onValueChange = { editGlobalObs = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("Globais") },
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 5
                        )

                        OutlinedTextField(
                            value = editParticularObs,
                            onValueChange = { editParticularObs = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("Particulares") },
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 5
                        )
                    }
                }
            }

            // Botão Guardar
            item {
                Button(
                    onClick = {
                        val dto = BeneficiaryPostDTO(
                            name = editName,
                            email = editEmail,
                            contact = editContact,
                            nif = editNif.toIntOrNull(),
                            street = editStreet,
                            number = editNumber.toIntOrNull(),
                            postalCode = editPostalcode,
                            studentNum = editStudentnum.toIntOrNull(),
                            course = editCourse,
                            curricularYear = editCurricularYear.toIntOrNull(),
                            globalObs = editGlobalObs,
                            particularObs = editParticularObs
                        )
                        onSave(dto)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Alterações", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

enum class SortColumn {
    USERNAME, STATUS, SCHEDULED_DATE
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

@Composable
fun BeneficiaryDeliveriesHist(
    deliveries: List<VDeliveryDTO>,
    isLoading: Boolean
) {
    var selectedDelivery by remember { mutableStateOf<VDeliveryDTO?>(null) }
    var sortColumn by remember { mutableStateOf(SortColumn.SCHEDULED_DATE) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }

    val sortedDeliveries = remember(deliveries, sortColumn, sortDirection) {
        val sorted = when (sortColumn) {
            SortColumn.USERNAME -> deliveries.sortedBy { it.userName ?: "" }
            SortColumn.STATUS -> deliveries.sortedBy { it.status }
            SortColumn.SCHEDULED_DATE -> deliveries.sortedBy { it.scheduledDate }
        }
        if (sortDirection == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Histórico de Entregas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Divider()

            Box(modifier = Modifier.weight(1f)) {
                if (deliveries.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sem entregas registadas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header da tabela
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TableHeader(
                                    text = "Utilizador",
                                    weight = 0.35f,
                                    sortColumn = SortColumn.USERNAME,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    onClick = {
                                        if (sortColumn == SortColumn.USERNAME) {
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING)
                                                SortDirection.DESCENDING else SortDirection.ASCENDING
                                        } else {
                                            sortColumn = SortColumn.USERNAME
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )

                                TableHeader(
                                    text = "Estado",
                                    weight = 0.3f,
                                    sortColumn = SortColumn.STATUS,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    onClick = {
                                        if (sortColumn == SortColumn.STATUS) {
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING)
                                                SortDirection.DESCENDING else SortDirection.ASCENDING
                                        } else {
                                            sortColumn = SortColumn.STATUS
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )

                                TableHeader(
                                    text = "Data",
                                    weight = 0.35f,
                                    sortColumn = SortColumn.SCHEDULED_DATE,
                                    currentSortColumn = sortColumn,
                                    sortDirection = sortDirection,
                                    onClick = {
                                        if (sortColumn == SortColumn.SCHEDULED_DATE) {
                                            sortDirection = if (sortDirection == SortDirection.ASCENDING)
                                                SortDirection.DESCENDING else SortDirection.ASCENDING
                                        } else {
                                            sortColumn = SortColumn.SCHEDULED_DATE
                                            sortDirection = SortDirection.ASCENDING
                                        }
                                    }
                                )
                            }
                        }

                        // Linhas da tabela
                        items(sortedDeliveries) { delivery ->
                            DeliveryRow(
                                delivery = delivery,
                                isSelected = selectedDelivery?.deliveryId == delivery.deliveryId,
                                onClick = { selectedDelivery = delivery }
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Área de observações
            if (selectedDelivery != null) {
                Divider()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 200.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Observações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedDelivery?.note ?: "Sem observações",
                            fontSize = 14.sp,
                            color = if (selectedDelivery?.note != null)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeader(
    text: String,
    weight: Float,
    sortColumn: SortColumn,
    currentSortColumn: SortColumn,
    sortDirection: SortDirection,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (currentSortColumn == sortColumn) {
            Icon(
                imageVector = if (sortDirection == SortDirection.ASCENDING)
                    Icons.Default.ArrowUpward
                else
                    Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DeliveryRow(
    delivery: VDeliveryDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = delivery.userName ?: "N/A",
            modifier = Modifier.weight(0.35f),
            fontSize = 14.sp
        )

        Text(
            text = delivery.status,
            modifier = Modifier.weight(0.3f),
            fontSize = 14.sp
        )

        Text(
            text = delivery.scheduledDate,
            modifier = Modifier.weight(0.35f),
            fontSize = 14.sp,
            textAlign = TextAlign.End
        )
    }
}