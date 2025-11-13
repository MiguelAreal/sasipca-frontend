package sasipca

import MainScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

/**
 * Método de inicialização da app.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {

    // Estado base da app
    val scope = rememberCoroutineScope()
    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    var selectedBeneficiaryId by remember { mutableStateOf<Int?>(null) }

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
 * Inicializa managers.
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

    // Inicializar sessão e tema
    LaunchedEffect(Unit) {
        onThemeLoaded(SettingsManager.isDarkTheme())

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
 *
 * Inicializa todos os Screens da aplicação.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedNavigation(
    currentScreen: Screen,
    selectedBeneficiaryId: Int?,
    onBeneficiarySelected: (Int) -> Unit,
    onThemeChange: (Boolean) -> Unit
) {
    val authRepository = ApiClient.authRepository
    val stockRepository = ApiClient.stockRepository
    val productRepository = ApiClient.productRepository
    val beneficiaryRepository = ApiClient.beneficiaryRepository

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
                stockRepository,
                productRepository,
                beneficiaryRepository,
                onOpenBeneficiary = { id ->
                    onBeneficiarySelected(id)
                    NavigationService.navigateTo(Screen.Beneficiary)
                }
            )

            Screen.Reception -> ReceptionScreen()
            Screen.Delivery -> DeliveryScreen()
            Screen.StockAdjustment -> PlaceholderScreen()
            Screen.Campaigns -> PlaceholderScreen()
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
                    stockRepository = stockRepository
                )
            }

            Screen.Settings -> SettingsScreen { onThemeChange(it) }
            Screen.Notifications -> PlaceholderScreen()
            Screen.Placeholder -> PlaceholderScreen()
            Screen.Calendar -> CalendarScreen(stockRepository)
            Screen.Home -> HomeScreen()
            Screen.Profile -> ProfileScreen()
            Screen.Products -> ProductsScreen(productRepository)
        }
    }
}

