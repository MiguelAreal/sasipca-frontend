package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun ReportsScreen(reportsRepository: ReportsRepository, beneficiaryRepository: BeneficiaryRepository) {
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
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // Posiciona o botão flutuante no canto inferior direito
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Header no topo
            Header("Relatórios", "Histórico e Geração")

            // --- Conteúdo Principal ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                if (uiState.reports.isEmpty() && !uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Nenhum relatório gerado ainda.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                LazyColumn(
                    // bottom padding de 80dp para evitar que o FAB cubra o último item
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
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
            onDismiss = { showDialog = false },
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
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
                Text(
                    text = report.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = report.reportTypeName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Gerado por: ${report.creatorName} em ${report.createdAt.take(10)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onDownload) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Transferir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}