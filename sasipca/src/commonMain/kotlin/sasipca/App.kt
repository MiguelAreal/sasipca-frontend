package sasipca

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import coil3.SingletonImageLoader
import kotlinx.coroutines.flow.collectLatest
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

    // O ecrã inicial é decidido dinamicamente
    var currentScreen by remember { mutableStateOf<cafe.adriel.voyager.core.screen.Screen?>(null) }

    // Inicialização da App
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope

        // Tenta carregar listas se estiver logado
        if (SessionManager.isLoggedInNow()) {
            try {
                ApiClient.listsRepository.loadLists()
                NotificationManager.refreshCount()
                currentScreen = MainScreen()
            } catch (e: Exception) {
                // Se falhar gravemente (sem net/servidor em baixo), força logout
                println("Erro critico na inicialização: ${e.message}")
                SessionManager.triggerLogout() // Isto emite o evento de logout
                SnackbarManager.show("Erro de conexão. Por favor, faça login novamente.", SnackbarType.ERROR)
            }
        } else {
            currentScreen = LoginScreen()
        }

        initialized = true

        // ESCUTAR O EVENTO DE LOGOUT FORÇADO
        // Se o token expirar durante o uso ou triggerLogout for chamado
        SessionManager.logoutEvent.collectLatest {
            currentScreen = LoginScreen()
        }
    }

    if (!initialized || currentScreen == null) {
        LoadingWidget()
        return
    }

    SasIpcaTheme(darkTheme = isDarkTheme) {
        // key(currentScreen) força o Navigator a recriar-se se formos "chutados" para o Login
        key(currentScreen) {
            Navigator(screen = currentScreen!!) { navigator ->
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
}