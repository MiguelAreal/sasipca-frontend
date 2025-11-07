package g8.ipca.sasipca.sasipca

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
import g8.ipca.sasipca.sasipca.navigation.NavigationService
import g8.ipca.sasipca.sasipca.navigation.Screen
import g8.ipca.sasipca.sasipca.screens.*
import g8.ipca.sasipca.sasipca.ui.components.CustomSnackbarHost
import g8.ipca.sasipca.sasipca.utils.SnackbarMessage
import g8.ipca.sasipca.sasipca.ui.theme.SasIpcaTheme
import g8.ipca.sasipca.sasipca.utils.SnackbarManager
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.repositories.AuthRepository
import g8.ipca.sasipca.sasipca.repositories.BeneficiaryRepository
import g8.ipca.sasipca.sasipca.repositories.ProductRepository
import g8.ipca.sasipca.sasipca.repositories.StockRepository
import g8.ipca.sasipca.sasipca.sasipca.screens.CalendarScreen
import g8.ipca.sasipca.sasipca.utils.SafeBackHandler
import g8.ipca.sasipca.sasipca.storage.SessionManager
import g8.ipca.sasipca.sasipca.storage.SettingsManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    val authRepository = remember { AuthRepository(ApiClient.client) }
    val stockRepository = remember { StockRepository(ApiClient.client) }
    val productRepository = remember { ProductRepository(ApiClient.client) }
    val beneficiaryRepository = remember { BeneficiaryRepository(ApiClient.client) }

    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    val scope = rememberCoroutineScope()
    var isDarkTheme by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // Inicializa Snackbar global
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
    }

    // 🚀 Verifica sessão ao iniciar
    LaunchedEffect(Unit) {

        isDarkTheme = SettingsManager.isDarkTheme()
        val hasValidToken = SessionManager.isAccessTokenValid()
        val refreshToken = SessionManager.getRefreshToken()

        if (hasValidToken) {
            NavigationService.resetTo(Screen.Main)
        } else if (refreshToken != null) {

            // tentar renovar token
            val result = authRepository.refreshToken()
            result.fold(
                onSuccess = {
                    NavigationService.resetTo(Screen.Main)
                },
                onFailure = {
                    SessionManager.clear()
                    NavigationService.resetTo(Screen.Login)
                }
            )
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
            enabled = NavigationService.canGoBack() && Screen.isOverlay(currentScreen)
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
                Screen.Login -> LoginScreen(authRepository)
                Screen.Main -> MainScreen(stockRepository,productRepository)
                Screen.Reception -> ReceptionScreen()
                Screen.Delivery -> DeliveryScreen()
                Screen.StockAdjustment -> PlaceholderScreen()
                Screen.Campaigns -> PlaceholderScreen()
                Screen.Beneficiaries -> BeneficiariesScreen(beneficiaryRepository)
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
