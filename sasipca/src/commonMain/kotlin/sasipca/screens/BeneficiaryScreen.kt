package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import sasipca.models.SnackbarType
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.DeliveryRepository
import sasipca.storage.ScreenSizeManager.isLargeScreen
import sasipca.ui.components.Header
import sasipca.ui.components.beneficiaries.*
import sasipca.utils.SnackbarManager
import sasipca.viewmodels.BeneficiaryDetailViewModel
import sasipca.viewmodels.DeliveriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryScreen(
    beneficiaryId: Int,
    repository: BeneficiaryRepository,
    deliveryRepository: DeliveryRepository,
    isReadOnly: Boolean = false
) {
    val navigator = LocalNavigator.currentOrThrow
    val beneficiaryViewModel = remember { BeneficiaryDetailViewModel(repository) }
    val deliveriesViewModel = remember { DeliveriesViewModel(deliveryRepository) }
    val uiState by beneficiaryViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val beneficiary = beneficiaryViewModel.getBeneficiary
    val isLoading by remember { beneficiaryViewModel::isLoading }
    val deliveries by deliveriesViewModel.deliveries.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Efeitos de carregamento e navegação permanecem iguais...
    LaunchedEffect(beneficiaryId) {
        beneficiaryViewModel.loadBeneficiary(beneficiaryId)
        deliveriesViewModel.loadBeneficiaryDeliveries(beneficiaryId)
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            beneficiaryViewModel.clearUiState()
            if (!isReadOnly) navigator.pop()
        }
        uiState.lastErrorMessage?.let {
            SnackbarManager.show(it, SnackbarType.ERROR)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            title = if (isReadOnly) "O Meu Perfil" else "Beneficiário",
            subTitle = beneficiary?.name ?: ""
        )

        // REMOVIDO: BoxWithConstraints.
        // USADO: Box simples para ocupar o resto do ecrã
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BeneficiaryEditForm(
                            beneficiary = beneficiary,
                            isLoading = isLoading,
                            errors = uiState.errors,
                            onSave = { body ->
                                if (!isReadOnly) {
                                    scope.launch { beneficiaryViewModel.updateBeneficiary(beneficiaryId, body) }
                                }
                            },
                            isReadOnly = isReadOnly
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        DeliveriesTable(
                            deliveries = deliveries,
                            isLoading = deliveriesViewModel.isLoading.collectAsState().value
                        )
                    }
                }
            } else {
                // Layout Mobile
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
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

                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                        when (selectedTab) {
                            0 -> BeneficiaryEditForm(
                                beneficiary = beneficiary,
                                isLoading = isLoading,
                                errors = uiState.errors,
                                onSave = { body ->
                                    if (!isReadOnly) {
                                        scope.launch { beneficiaryViewModel.updateBeneficiary(beneficiaryId, body) }
                                    }
                                },
                                isReadOnly = isReadOnly
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