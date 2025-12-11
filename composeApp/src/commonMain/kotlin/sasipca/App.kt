package sasipca

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import coil3.SingletonImageLoader
import kotlinx.coroutines.CoroutineScope
import sasipca.models.SnackbarMessage
import sasipca.models.SnackbarType
import sasipca.network.ApiClient
import sasipca.screens.navigation.LoginScreen
import sasipca.screens.navigation.MainScreen
import sasipca.storage.NotificationManager
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.components.CustomSnackbarHost
import sasipca.ui.components.LoadingWidget
import sasipca.ui.theme.SasIpcaTheme
import sasipca.utils.SnackbarManager
import sasipca.utils.getAsyncImageLoader

@Composable
fun App() {
    SingletonImageLoader.setSafe { context -> getAsyncImageLoader(context) }

    val scope = rememberCoroutineScope()
    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    var initialized by remember { mutableStateOf(false) }

    val isDarkTheme by SettingsManager.isDarkThemeFlow.collectAsState()

    // Determina o ecrã inicial
    var startScreen by remember { mutableStateOf<cafe.adriel.voyager.core.screen.Screen?>(null) }

    InitializeApp(
        onInitDone = { screen ->
            startScreen = screen
            initialized = true
        },
        snackbarState = snackbarState,
        scope = scope
    )

    if (!initialized || startScreen == null) {
        LoadingWidget()
        return
    }

    // O tema atualiza-se automaticamente porque 'isDarkTheme' mudou
    SasIpcaTheme(darkTheme = isDarkTheme) {
        Navigator(screen = startScreen!!) { navigator ->
            SlideTransition(navigator) { screen ->
                screen.Content()
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

@Composable
private fun InitializeApp(
    onInitDone: (cafe.adriel.voyager.core.screen.Screen) -> Unit,
    snackbarState: MutableState<SnackbarMessage?>,
    scope: CoroutineScope
) {
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope

        try {
            ApiClient.listsRepository.loadLists()
        } catch (e: Exception) {
            SnackbarManager.show("Falha ao carregar listas.", SnackbarType.ERROR)
        }

        val refreshToken = SessionManager.getRefreshToken()
        val initialScreen = if (refreshToken != null) MainScreen() else LoginScreen()

        NotificationManager.refreshCount()
        onInitDone(initialScreen)
    }
}