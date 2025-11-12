package sasipca

import MainScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen
import sasipca.ui.components.CustomSnackbarHost
import sasipca.utils.SnackbarMessage
import sasipca.ui.theme.SasIpcaTheme
import sasipca.utils.SnackbarManager
import sasipca.screens.CalendarScreen
import sasipca.utils.SafeBackHandler
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.screens.BeneficiariesScreen
import sasipca.screens.BeneficiaryScreen
import sasipca.screens.DeliveryScreen
import sasipca.screens.HomeScreen
import sasipca.screens.LoginScreen
import sasipca.screens.PlaceholderScreen
import sasipca.screens.ProductsScreen
import sasipca.screens.ProfileScreen
import sasipca.screens.ReceptionScreen
import sasipca.screens.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {

    val authRepository = ApiClient.authRepository
    val stockRepository = ApiClient.stockRepository
    val productRepository = ApiClient.productRepository
    val beneficiaryRepository = ApiClient.beneficiaryRepository

    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    val scope = rememberCoroutineScope()
    var isDarkTheme by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // Armazenar o beneficiário selecionado
    var selectedBeneficiaryId by remember { mutableStateOf<Int?>(null) }


    // Inicializa Snackbar global
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
    }

    LaunchedEffect(Unit) {

        isDarkTheme = SettingsManager.isDarkTheme()
        val refreshToken = SessionManager.getRefreshToken()

        if (refreshToken != null) {
            NavigationService.resetTo(Screen.Main)
        } else {
            NavigationService.resetTo(Screen.Login)
        }

        initialized = true
    }

    val currentScreen = NavigationService.currentScreen

    // Espera até terminar a validação inicial
    if (!initialized) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    SasIpcaTheme(darkTheme = isDarkTheme) {
        SafeBackHandler(
            enabled = NavigationService.canGoBack() && Screen.Companion.isOverlay(currentScreen)
        ) {
            NavigationService.goBack()
        }

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
                Screen.Login -> LoginScreen(authRepository) // Passa o singleton
                Screen.Main -> MainScreen(stockRepository,
                    productRepository,
                    beneficiaryRepository,
                    onOpenBeneficiary = { id ->
                        selectedBeneficiaryId = id
                        NavigationService.navigateTo(Screen.Beneficiary)
                    })

                Screen.Reception -> ReceptionScreen()
                Screen.Delivery -> DeliveryScreen()
                Screen.StockAdjustment -> PlaceholderScreen()
                Screen.Campaigns -> PlaceholderScreen()
                Screen.Beneficiaries -> BeneficiariesScreen(
                    beneficiaryRepository,
                    onOpenBeneficiary = { id ->
                        selectedBeneficiaryId = id
                        NavigationService.navigateTo(Screen.Beneficiary)
                    }
                )
                Screen.Beneficiary -> {
                    selectedBeneficiaryId?.let { id ->
                        BeneficiaryScreen(
                            beneficiaryId = id,
                            repository = beneficiaryRepository,
                            stockRepository = stockRepository
                        )
                    }
                }
                Screen.Settings -> SettingsScreen { isDark -> isDarkTheme = isDark }
                Screen.Notifications -> PlaceholderScreen()
                Screen.Placeholder -> PlaceholderScreen()
                Screen.Calendar -> CalendarScreen(stockRepository)
                Screen.Home -> HomeScreen()
                Screen.Profile -> ProfileScreen()
                Screen.Products -> ProductsScreen(productRepository)
            }
        }

        CustomSnackbarHost(
            snackbarMessageState = snackbarState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}