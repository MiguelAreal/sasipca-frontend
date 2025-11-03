package g8.ipca.sasipca.sasipca

import MainScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import g8.ipca.sasipca.sasipca.navigation.NavigationService
import g8.ipca.sasipca.sasipca.navigation.Screen
import g8.ipca.sasipca.sasipca.screens.*
import g8.ipca.sasipca.sasipca.ui.components.CustomSnackbarHost
import g8.ipca.sasipca.sasipca.ui.components.SnackbarMessage
import g8.ipca.sasipca.sasipca.ui.theme.SasIpcaTheme
import g8.ipca.sasipca.sasipca.utils.SnackbarManager
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.repositories.AuthRepository

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    val authRepository = remember { AuthRepository(ApiClient.client) }
    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    val scope = rememberCoroutineScope()
    var isDarkTheme by remember { mutableStateOf(false) }

    // Configuração global
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
    }

    val currentScreen = NavigationService.currentScreen
    val previousScreen = NavigationService.previousScreen

    SasIpcaTheme(darkTheme = isDarkTheme) {
        Scaffold { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues)) {

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { it / 2 },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)) with
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 2 },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                    },
                    label = "navigation_transition"

                ) { screen ->
                    when (screen) {
                        Screen.Login -> LoginScreen(
                            authRepository = authRepository
                        )
                        Screen.Main -> MainScreen()
                        Screen.Reception -> ReceptionScreen()
                        Screen.Delivery -> DeliveryScreen()
                        Screen.StockAdjustment -> PlaceholderScreen()
                        Screen.Campaigns -> PlaceholderScreen()
                        Screen.Beneficiaries -> BeneficiariesScreen()
                        Screen.Settings -> SettingsScreen(
                            onThemeChanged = { isDark -> isDarkTheme = isDark }
                        )
                        Screen.Notifications -> PlaceholderScreen()
                        Screen.Placeholder -> PlaceholderScreen()

                        // Tabs dentro de MainScreen.
                        Screen.Calendar -> CalendarScreen()
                        Screen.Home -> HomeScreen()
                        Screen.Profile -> ProfileScreen()
                        Screen.Stock -> StockScreen()


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
    }
}
