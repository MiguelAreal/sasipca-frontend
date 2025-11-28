package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import sasipca.ApiClient
import sasipca.models.BeneficiaryItem
import sasipca.models.ReportFormat
import sasipca.models.ReportGetDTO
import sasipca.models.ReportTypesEnum
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.ReportsRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.ValidatedDateField
import sasipca.utils.PlatformFileSaver
import sasipca.utils.SnackbarManager
import sasipca.utils.SnackbarType
import sasipca.viewmodels.BeneficiariesViewModel
import sasipca.viewmodels.ReportsViewModel

@Composable
fun ReportsScreen(reportsRepository : ReportsRepository, beneficiaryRepository : BeneficiaryRepository) {
    // Inicialização dos ViewModels
    val viewModel = remember { ReportsViewModel(reportsRepository, PlatformFileSaver()) }
    val beneficiariesViewModel = remember { BeneficiariesViewModel(beneficiaryRepository) }

    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    // Gestão de Snackbars
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            SnackbarManager.show(it, SnackbarType.ERROR)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            SnackbarManager.show(it, SnackbarType.SUCCESS)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Relatório", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Header("Relatórios", "Histórico e Geração")

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.reports.isEmpty() && !uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum relatório gerado ainda.", color = Color.Gray)
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.reports.sortedByDescending { it.createdAt }) { report ->
                        ReportItemCard(
                            report = report,
                            onDownload = { viewModel.downloadExistingReport(report) }
                        )
                    }
                }

                if (uiState.isLoading) {
                    LoadingWidget()
                }
            }
        }
    }

    if (showDialog) {
        GenerateReportDialog(
            beneficiariesViewModel = beneficiariesViewModel, // Passamos o VM para o Dialog
            onDismiss = { showDialog = false },
            onGenerate = { type, format, name, start, end, movId, status, beneId ->
                // Mapear os argumentos para o ViewModel
                viewModel.generateNewReport(
                    type = type,
                    format = format,
                    fileName = name,
                    startDate = start,
                    endDate = end,
                    movementId = movId,
                    status = status,
                    beneficiaryId = beneId
                )
                showDialog = false
            }
        )
    }
}

@Composable
fun ReportItemCard(report: ReportGetDTO, onDownload: () -> Unit) {
    val isCsv = report.name.endsWith(".csv", true)
    val icon = if (isCsv) Icons.Default.TableChart else Icons.Default.PictureAsPdf
    val color = if (isCsv) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(report.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(report.reportTypeName, fontSize = 12.sp, color = Color.Gray)
                Text("Gerado por: ${report.creatorName} em ${report.createdAt.take(10)}", fontSize = 12.sp)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = "Transferir")
            }
        }
    }
}

// --- DIÁLOGO DE CRIAÇÃO OTIMIZADO ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateReportDialog(
    beneficiariesViewModel: BeneficiariesViewModel,
    onDismiss: () -> Unit,
    // Assinatura atualizada para incluir Status e BeneficiaryId
    onGenerate: (ReportTypesEnum, ReportFormat, String, String?, String?, String?, Int?, Int?) -> Unit
) {
    // Estados Básicos
    var selectedType by remember { mutableStateOf(ReportTypesEnum.MovementHeaders) }
    var typeExpanded by remember { mutableStateOf(false) }

    var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }
    var fileName by remember { mutableStateOf("Relatorio_${kotlinx.datetime.Clock.System.now().epochSeconds}") }

    // Estados Filtros Genéricos
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // Filtros Específicos
    var movementId by remember { mutableStateOf("") }

    // Filtro Status Entrega (Map local para dropdown)
    val deliveryStatuses = mapOf(
        null to "Todos",
        1 to "Agendada",
        2 to "Entregue",
        3 to "Cancelada"
    )
    var selectedStatus by remember { mutableStateOf<Int?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }

    // --- LÓGICA DE BENEFICIÁRIO (Copiada e adaptada do DeliveryScreen) ---
    var beneficiaryQuery by remember { mutableStateOf("") }
    var selectedBeneficiary by remember { mutableStateOf<BeneficiaryItem?>(null) }
    var isBeneficiaryDropdownExpanded by remember { mutableStateOf(false) }
    val beneficiarySearchResults by remember { beneficiariesViewModel::beneficiaries }
    val isBeneficiaryLoading by remember { beneficiariesViewModel::isLoading }
    val beneficiaryFocusRequester = remember { FocusRequester() }

    LaunchedEffect(beneficiaryQuery) {
        if (selectedBeneficiary == null || beneficiaryQuery != selectedBeneficiary?.name) {
            if (selectedBeneficiary != null && beneficiaryQuery != selectedBeneficiary?.name) {
                selectedBeneficiary = null
            }
            if (beneficiaryQuery.length >= 2) {
                delay(250)
                beneficiariesViewModel.loadBeneficiaries(search = beneficiaryQuery)
                isBeneficiaryDropdownExpanded = true
            } else {
                isBeneficiaryDropdownExpanded = false
            }
        }
    }
    // ----------------------------------------------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Relatório") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Importante para ecrãs pequenos
            ) {

                // 1. SELEÇÃO DO TIPO DE RELATÓRIO (DROPDOWN)
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Relatório") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ReportTypesEnum.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label()) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. NOME DO FICHEIRO
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nome do Ficheiro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()

                Text("Filtros", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                // --- FILTROS DINÂMICOS ---

                // Caso 1: MOVEMENT_DETAILS (Só precisa de ID)
                if (selectedType == ReportTypesEnum.MovementDetails) {
                    OutlinedTextField(
                        value = movementId,
                        onValueChange = { if (it.all { c -> c.isDigit() }) movementId = it },
                        label = { Text("ID do Movimento (Obrigatório)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Caso 2 e 3: Cabeçalhos (Movimentos ou Entregas) usam Datas
                else {
                    if (isLargeScreen()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ValidatedDateField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = "Data Início",
                                modifier = Modifier.weight(1f)
                            )
                            ValidatedDateField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = "Data Fim",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ValidatedDateField(value = startDate, onValueChange = { startDate = it }, label = "Data Início", modifier = Modifier.fillMaxWidth())
                        ValidatedDateField(value = endDate, onValueChange = { endDate = it }, label = "Data Fim", modifier = Modifier.fillMaxWidth())
                    }

                    // Caso 3: DELIVERY_HEADERS (Tem Status e Beneficiário extra)
                    if (selectedType == ReportTypesEnum.DeliveryHeaders) {

                        // Status Dropdown
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = deliveryStatuses[selectedStatus] ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Estado da Entrega") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false }
                            ) {
                                deliveryStatuses.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedStatus = key
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Beneficiário Autocomplete
                        ExposedDropdownMenuBox(
                            expanded = isBeneficiaryDropdownExpanded,
                            onExpandedChange = {
                                isBeneficiaryDropdownExpanded = it
                                if(it) beneficiaryFocusRequester.requestFocus()
                            }
                        ) {
                            OutlinedTextField(
                                value = beneficiaryQuery,
                                onValueChange = {
                                    beneficiaryQuery = it
                                    if (selectedBeneficiary != null && it != selectedBeneficiary?.name) {
                                        selectedBeneficiary = null
                                    }
                                    beneficiaryFocusRequester.requestFocus()
                                },
                                label = { Text("Beneficiário (Opcional)") },
                                placeholder = { Text("Pesquisar nome...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .focusRequester(beneficiaryFocusRequester),
                                trailingIcon = {
                                    if (isBeneficiaryLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = isBeneficiaryDropdownExpanded,
                                onDismissRequest = { isBeneficiaryDropdownExpanded = false },
                                modifier = Modifier.exposedDropdownSize(true),
                                properties = PopupProperties(focusable = false)
                            ) {
                                val list = beneficiarySearchResults?.data ?: emptyList()
                                if (list.isEmpty() && !isBeneficiaryLoading) {
                                    DropdownMenuItem(text = { Text("Sem resultados") }, onClick = {}, enabled = false)
                                } else {
                                    list.forEach { beneficiary ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(beneficiary.name, style = MaterialTheme.typography.bodyLarge)
                                                    Text(beneficiary.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedBeneficiary = beneficiary
                                                beneficiaryQuery = beneficiary.name
                                                isBeneficiaryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider()

                // 3. FORMATO DE SAÍDA
                Text("Formato", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = (selectedFormat == format),
                            onClick = { selectedFormat = format },
                            label = { Text(format.name) },
                            leadingIcon = {
                                if (selectedFormat == format) Icon(Icons.Default.Check, null)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(
                        selectedType,
                        selectedFormat,
                        fileName,
                        startDate.ifBlank { null },
                        endDate.ifBlank { null },
                        movementId.ifBlank { null },
                        selectedStatus,
                        selectedBeneficiary?.beneficiaryId
                    )
                },
                // Validação do botão Gerar
                enabled = fileName.isNotBlank() &&
                        (selectedType != ReportTypesEnum.MovementDetails || movementId.isNotBlank())
            ) {
                Text("Gerar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}