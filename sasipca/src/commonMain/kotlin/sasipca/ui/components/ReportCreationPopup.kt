package sasipca.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import sasipca.models.BeneficiaryItem
import sasipca.models.ReportFormat
import sasipca.models.ReportTypesEnum
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.ValidatedDateField
import sasipca.viewmodels.BeneficiariesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCreationPopup(
    beneficiariesViewModel: BeneficiariesViewModel,
    onDismiss: () -> Unit,
    onGenerate: (ReportTypesEnum, ReportFormat, String, String?, String?, String?, Int?, Int?) -> Unit,
    // Parâmetros Opcionais para Contexto Específico
    presetMovementId: Int? = null
) {
    // Se tivermos um ID predefinido, forçamos o tipo MovementDetails, senão default
    val initialType = if (presetMovementId != null) ReportTypesEnum.MovementDetails else ReportTypesEnum.MovementHeaders

    var selectedType by remember { mutableStateOf(initialType) }
    var typeExpanded by remember { mutableStateOf(false) }

    var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }

    // Nome sugerido automático
    val defaultName = if(presetMovementId != null) "Movimento_${presetMovementId}_${kotlinx.datetime.Clock.System.now().epochSeconds}"
    else "Relatorio_${kotlinx.datetime.Clock.System.now().epochSeconds}"

    var fileName by remember { mutableStateOf(defaultName) }

    // Filtros
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var movementId by remember { mutableStateOf(presetMovementId?.toString() ?: "") }

    // Dropdown Status
    val deliveryStatuses = mapOf(null to "Todos", 1 to "Agendada", 2 to "Entregue", 3 to "Cancelada")
    var selectedStatus by remember { mutableStateOf<Int?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }

    // Lógica de Beneficiários
    var beneficiaryQuery by remember { mutableStateOf("") }
    var selectedBeneficiary by remember { mutableStateOf<BeneficiaryItem?>(null) }
    var isBeneficiaryDropdownExpanded by remember { mutableStateOf(false) }
    val beneficiarySearchResults by remember { beneficiariesViewModel::beneficiaries }
    val isBeneficiaryLoading by remember { beneficiariesViewModel::isLoading }
    val beneficiaryFocusRequester = remember { FocusRequester() }

    LaunchedEffect(beneficiaryQuery) {
        if (selectedBeneficiary == null || beneficiaryQuery != selectedBeneficiary?.name) {
            if (beneficiaryQuery.length >= 2) {
                delay(250)
                beneficiariesViewModel.loadBeneficiaries(search = beneficiaryQuery)
                isBeneficiaryDropdownExpanded = true
            } else {
                isBeneficiaryDropdownExpanded = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (presetMovementId != null) "Relatório de Movimento" else "Novo Relatório") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                // 1. SELEÇÃO DO TIPO (Só mostra dropdown se NÃO for preset)
                if (presetMovementId == null) {
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                        OutlinedTextField(
                            value = selectedType.label(), onValueChange = {}, readOnly = true,
                            label = { Text("Tipo de Relatório") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            // Filtramos MovementDetails da lista geral, pois agora é acessível via histórico
                            ReportTypesEnum.entries.filter { it != ReportTypesEnum.MovementDetails }.forEach { type ->
                                DropdownMenuItem(text = { Text(type.label()) }, onClick = { selectedType = type; typeExpanded = false })
                            }
                        }
                    }
                } else {
                    // Campo Read-Only mostrando o tipo
                    OutlinedTextField(
                        value = "Detalhe de Movimento",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Tipo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. NOME DO FICHEIRO
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nome do Ficheiro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. FILTROS DINÂMICOS
                // Se for detalhe de movimento (via preset ou seleção manual antiga se existir)
                if (selectedType != ReportTypesEnum.MovementDetails) {
                    HorizontalDivider()
                    if (isLargeScreen()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ValidatedDateField(value = startDate, onValueChange = { startDate = it }, label = "Data Início", modifier = Modifier.weight(1f))
                            ValidatedDateField(value = endDate, onValueChange = { endDate = it }, label = "Data Fim", modifier = Modifier.weight(1f))
                        }
                    } else {
                        ValidatedDateField(value = startDate, onValueChange = { startDate = it }, label = "Data Início", modifier = Modifier.fillMaxWidth())
                        ValidatedDateField(value = endDate, onValueChange = { endDate = it }, label = "Data Fim", modifier = Modifier.fillMaxWidth())
                    }

                    if (selectedType == ReportTypesEnum.DeliveryHeaders) {
                        // Dropdown Status
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

                        // Autocomplete Beneficiário
                        ExposedDropdownMenuBox(
                            expanded = isBeneficiaryDropdownExpanded,
                            onExpandedChange = { isBeneficiaryDropdownExpanded = it; if(it) beneficiaryFocusRequester.requestFocus() }
                        ) {
                            OutlinedTextField(
                                value = beneficiaryQuery,
                                onValueChange = { beneficiaryQuery = it; beneficiaryFocusRequester.requestFocus() },
                                label = { Text("Beneficiário (Opcional)") },
                                modifier = Modifier.fillMaxWidth().menuAnchor().focusRequester(beneficiaryFocusRequester),
                                trailingIcon = { if (isBeneficiaryLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Icon(Icons.Default.Search, null) }
                            )
                            DropdownMenu(
                                expanded = isBeneficiaryDropdownExpanded,
                                onDismissRequest = { isBeneficiaryDropdownExpanded = false },
                                modifier = Modifier.exposedDropdownSize(true),
                                properties = PopupProperties(focusable = false)
                            ) {
                                beneficiarySearchResults?.data?.forEach { bene ->
                                    DropdownMenuItem(
                                        text = { Text(bene.name) },
                                        onClick = { selectedBeneficiary = bene; beneficiaryQuery = bene.name; isBeneficiaryDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Divider()

                // 4. FORMATO
                Text("Formato", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = (selectedFormat == format),
                            onClick = { selectedFormat = format },
                            label = { Text(format.name) },
                            leadingIcon = { if (selectedFormat == format) Icon(Icons.Default.Check, null) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(
                        selectedType, selectedFormat, fileName,
                        startDate.ifBlank { null }, endDate.ifBlank { null },
                        movementId.ifBlank { null }, selectedStatus, selectedBeneficiary?.beneficiaryId
                    )
                },
                enabled = fileName.isNotBlank() && (selectedType != ReportTypesEnum.MovementDetails || movementId.isNotBlank())
            ) { Text("Gerar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}