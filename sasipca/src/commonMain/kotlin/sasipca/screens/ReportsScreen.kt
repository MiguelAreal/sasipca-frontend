package sasipca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.ReportGetDTO
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.ReportsRepository
import sasipca.ui.components.Header
import sasipca.ui.components.LoadingWidget
import sasipca.utils.PlatformFileSaver
import sasipca.utils.SnackbarManager
import sasipca.models.SnackbarType
import sasipca.ui.components.ReportCreationPopup
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
        ReportCreationPopup(
            beneficiariesViewModel = beneficiariesViewModel,
            onDismiss = { },
            onGenerate = { type, format, name, start, end, movId, status, beneId ->
                viewModel.generateNewReport(
                    type = type, format = format, fileName = name,
                    startDate = start, endDate = end, movementId = movId,
                    status = status, beneficiaryId = beneId
                )
            },
            presetMovementId = null
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

