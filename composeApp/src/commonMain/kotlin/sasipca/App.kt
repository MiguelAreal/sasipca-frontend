package sasipca

import MainScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import kotlinx.coroutines.CoroutineScope
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen
import sasipca.screens.*
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.components.CustomSnackbarHost
import sasipca.ui.components.LoadingWidget
import sasipca.ui.theme.SasIpcaTheme
import sasipca.utils.HandleBackNavigation
import sasipca.utils.SnackbarManager
import sasipca.utils.SnackbarMessage
import sasipca.utils.SnackbarType
import sasipca.utils.getAsyncImageLoader

/**
 * Método de inicialização da app.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {

    SingletonImageLoader.setSafe { context ->
        getAsyncImageLoader(context)
    }

    // Estado base da app
    val scope = rememberCoroutineScope()
    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    var selectedBeneficiaryId by remember { mutableStateOf<Int?>(null) }
    var selectedProductBarcode by remember { mutableStateOf<String?>(null) }

    // Inicializar tema e singletons globais
    InitializeApp(
        onInitDone = { initialized = true },
        onThemeLoaded = { isDarkTheme = it },
        snackbarState = snackbarState,
        scope = scope
    )

    // Espera até a inicialização completar, mostrando widget.
    if (!initialized) {
        LoadingWidget()
        return
    }

    // Observa navegação e faz build do conteúdo da app
    val currentScreen = NavigationService.currentScreen

    SasIpcaTheme(darkTheme = isDarkTheme) {
        HandleBackNavigation(currentScreen)
        AnimatedNavigation(
            currentScreen = currentScreen,
            selectedBeneficiaryId = selectedBeneficiaryId,
            onBeneficiarySelected = { selectedBeneficiaryId = it },
            selectedProductBarcode = selectedProductBarcode,
            onProductSelected = { selectedProductBarcode = it },
            onThemeChange = { isDarkTheme = it }
        )

        CustomSnackbarHost(
            snackbarMessageState = snackbarState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

/**
 * Inicializa managers e carrega dados essenciais.
 */
@Composable
private fun InitializeApp(
    onInitDone: () -> Unit,
    onThemeLoaded: (Boolean) -> Unit,
    snackbarState: MutableState<SnackbarMessage?>,
    scope: CoroutineScope
) {
    // Inicializar Snackbar
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
    }

    // Inicializar sessão, tema e carregar listas
    LaunchedEffect(Unit) {
        onThemeLoaded(SettingsManager.isDarkTheme())

        try {
            ApiClient.listsRepository.loadLists()
        } catch (e: Exception) {
            // Não bloqueia a app, mas avisa o utilizador
            SnackbarManager.show("Falha ao carregar listas auxiliares.", SnackbarType.ERROR)
        }

        // 3. Verificar Sessão
        val refreshToken = SessionManager.getRefreshToken()
        if (refreshToken != null) {
            NavigationService.resetTo(Screen.Main)
        } else {
            NavigationService.resetTo(Screen.Login)
        }

        onInitDone()
    }
}


/**
 * Navegação animada entre páginas.
 * Inicializa todos os Screens da aplicação.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedNavigation(
    currentScreen: Screen,
    selectedBeneficiaryId: Int?,
    onBeneficiarySelected: (Int) -> Unit,
    selectedProductBarcode: String?,
    onProductSelected: (String) -> Unit,
    onThemeChange: (Boolean) -> Unit
) {
    // Obter referências dos repositórios já inicializados no ApiClient
    val authRepository = ApiClient.authRepository
    val deliveryRepository = ApiClient.deliveryRepository
    val receiptRepository = ApiClient.receiptRepository
    val productRepository = ApiClient.productRepository
    val beneficiaryRepository = ApiClient.beneficiaryRepository
    val campaignRepository = ApiClient.campaignRepository
    val listsRepository = ApiClient.listsRepository
    val reportsRepository = ApiClient.reportRepository
    val historyRepository = ApiClient.historyRepository
    val notificationRepository = ApiClient.notificationRepository

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { it / 2 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))).togetherWith(
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            )
        },
        label = "navigation_transition"
    ) { screen ->
        when (screen) {
            Screen.Login -> LoginScreen(authRepository)
            Screen.Main -> MainScreen(
                deliveryRepository,
                productRepository,
                beneficiaryRepository,
                campaignRepository,
                onOpenBeneficiary = { id ->
                    onBeneficiarySelected(id)
                    NavigationService.navigateTo(Screen.Beneficiary)
                },
                onOpenProduct = { barcode ->
                    onProductSelected(barcode)
                    NavigationService.navigateTo(Screen.Product)}
            )
            Screen.History -> HistoryScreen(historyRepository)

            Screen.Reception -> ReceiptScreen(productRepository, receiptRepository)
            Screen.Delivery -> DeliveryScreen(productRepository, deliveryRepository, beneficiaryRepository)
            Screen.StockAdjustment -> PlaceholderScreen()
            Screen.Campaigns -> CampaignsScreen(campaignRepository, listsRepository)

            Screen.Beneficiaries -> BeneficiariesScreen(
                beneficiaryRepository,
                onOpenBeneficiary = { id ->
                    onBeneficiarySelected(id)
                    NavigationService.navigateTo(Screen.Beneficiary)
                }
            )
            Screen.Beneficiary -> selectedBeneficiaryId?.let { id ->
                BeneficiaryScreen(
                    beneficiaryId = id,
                    repository = beneficiaryRepository,
                    deliveryRepository = deliveryRepository
                )
            }
            Screen.Reports -> ReportsScreen(reportsRepository,beneficiaryRepository)
            Screen.Settings -> SettingsScreen { onThemeChange(it) }
            Screen.Notifications -> PlaceholderScreen()
            Screen.Placeholder -> PlaceholderScreen()
            Screen.Calendar -> CalendarScreen(deliveryRepository)
            Screen.Home -> HomeScreen()
            Screen.Profile -> ProfileScreen()
            Screen.Products -> ProductsScreen(
                productRepository,
                onOpenProduct = { barcode ->
                    onProductSelected(barcode)
                    NavigationService.navigateTo(Screen.Product)
                }
            )
            Screen.Product -> selectedProductBarcode?.let { barcode ->
                ProductScreen(
                    barcode = barcode,
                    productRepository = productRepository,
                )
            }
        }
    }
}