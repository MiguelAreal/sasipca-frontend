package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sasipca.navigation.NavigationService
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.DeliveryRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.storage.ScreenSizeManager.updateSize
import sasipca.ui.components.Header
import sasipca.ui.components.beneficiaries.*
import sasipca.viewmodels.BeneficiaryDetailViewModel
import sasipca.viewmodels.DeliveriesViewModel

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryScreen(
    beneficiaryId: Int,
    repository: BeneficiaryRepository,
    deliveryRepository: DeliveryRepository
) {

    val viewModel = remember { BeneficiaryDetailViewModel(repository) }
    val deliveriesViewModel = remember { DeliveriesViewModel(deliveryRepository) }
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            title = "Beneficiário",
            subTitle = beneficiary?.name ?: ""
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
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
                            onSave = { body ->
                                scope.launch {
                                    viewModel.updateBeneficiary(beneficiaryId, body)
                                    NavigationService.goBack()
                                }
                            }
                        )
                    }

                    // Coluna da direita - Histórico
                    Box(modifier = Modifier.weight(1f)) {
                        DeliveriesTable(
                            deliveries = deliveries,
                            isLoading = deliveriesViewModel.isLoading.collectAsState().value
                        )
                    }
                }
            } else {
                // Layout com separadores para ecrãs pequenos
                Column(modifier = Modifier.fillMaxSize()) {

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,

                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Perfil") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Histórico") }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize().
                    padding(horizontal = 20.dp)
                    ) {
                        when (selectedTab) {
                            0 -> BeneficiaryEditForm(
                                beneficiary = beneficiary,
                                isLoading = isLoading,
                                onSave = { body ->
                                    scope.launch {
                                        viewModel.updateBeneficiary(beneficiaryId, body)
                                        NavigationService.goBack()
                                    }
                                }
                            )
                            1 -> DeliveriesTable(
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



