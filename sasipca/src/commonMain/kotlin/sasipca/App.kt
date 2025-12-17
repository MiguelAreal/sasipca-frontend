package sasipca

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import coil3.SingletonImageLoader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sasipca.models.SnackbarMessage
import sasipca.network.ApiClient
import sasipca.screens.navigation.LoginScreen
import sasipca.screens.navigation.MainScreen
import sasipca.storage.ListsStore
import sasipca.storage.NotificationManager
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.components.CustomSnackbarHost
import sasipca.ui.components.LoadingWidget
import sasipca.ui.theme.SasIpcaTheme
import sasipca.utils.SnackbarManager
import sasipca.utils.getAsyncImageLoader

@Composable
fun App(openCalendar: Boolean = false) {
    SingletonImageLoader.setSafe { context -> getAsyncImageLoader(context) }

    val scope = rememberCoroutineScope()
    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    val isDarkTheme by SettingsManager.isDarkThemeFlow.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var initError by remember { mutableStateOf<String?>(null) }
    var currentScreen by remember { mutableStateOf<cafe.adriel.voyager.core.screen.Screen?>(null) }

    // Função de Inicialização Robusta
    suspend fun initializeData() {
        isLoading = true
        initError = null

        try {
            if (SessionManager.isLoggedInNow()) {
                ApiClient.listsRepository.loadLists()

                if (!ListsStore.isInitialized) {
                    throw Exception("Dados incompletos. As listas não foram carregadas corretamente.")
                }

                NotificationManager.refreshCount()
                currentScreen = MainScreen(openCalendar = openCalendar)

            } else {
                currentScreen = LoginScreen()
            }
        } catch (e: Exception) {
            println("Erro na inicialização: ${e.message}")

            // Se for erro de autenticação (401), faz logout forçado
            if (e.message?.contains("401") == true) {
                SessionManager.triggerLogout()
            } else {
                // Qualquer outro erro (net, dados vazios), mostra o ecrã de Retry
                initError = "Não foi possível carregar os dados essenciais.\nVerifique a sua conexão."
            }
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
        initializeData()

        SessionManager.logoutEvent.collectLatest {
            currentScreen = LoginScreen()
            initError = null
        }
    }

    SasIpcaTheme(darkTheme = isDarkTheme) {
        if (isLoading) {
            LoadingWidget()
        } else if (initError != null) {
            // ECRÃ DE ERRO / RETRY
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = initError!!,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Botão Tentar Novamente
                    Button(
                        onClick = { scope.launch { initializeData() } }
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tentar Novamente")
                    }

                    TextButton(
                        onClick = {
                            scope.launch { SessionManager.triggerLogout() }
                        }
                    ) {
                        Text("Sair / Logout")
                    }
                }
            }
        } else if (currentScreen != null) {
            key(currentScreen) {
                Navigator(screen = currentScreen!!) { navigator ->
                    SlideTransition(navigator) { screen -> screen.Content() }
                    CustomSnackbarHost(
                        snackbarMessageState = snackbarState,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
        }
    }
}