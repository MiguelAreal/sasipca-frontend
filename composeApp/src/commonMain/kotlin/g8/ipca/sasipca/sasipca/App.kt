package g8.ipca.sasipca.sasipca

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.repositories.AuthRepository
import g8.ipca.sasipca.sasipca.screens.*
import g8.ipca.sasipca.sasipca.storage.SettingsManager
import g8.ipca.sasipca.sasipca.ui.components.CustomSnackbarHost
import g8.ipca.sasipca.sasipca.ui.components.SnackbarMessage
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import g8.ipca.sasipca.sasipca.ui.theme.SasIpcaTheme
import g8.ipca.sasipca.sasipca.ui.utils.SnackbarManager

enum class Screens {
    LOGIN,
    DASHBOARD
}

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    val authRepository = remember { AuthRepository(ApiClient.client) }
    var currentScreen by remember { mutableStateOf(Screens.LOGIN) }
    var isDarkTheme by remember { mutableStateOf(SettingsManager.isDarkTheme()) }
    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    val scope = rememberCoroutineScope()

    // Inicializa o gestor global
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
    }

    SasIpcaTheme(darkTheme = isDarkTheme) {
        Scaffold { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Conteúdo principal
                when (currentScreen) {
                    Screens.LOGIN -> LoginScreen(
                        authRepository = authRepository,
                        onLoginSuccess = { currentScreen = Screens.DASHBOARD }
                    )
                    Screens.DASHBOARD -> MainScreen(
                        onThemeChanged = { isDark -> isDarkTheme = isDark }
                    )
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