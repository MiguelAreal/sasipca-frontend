package sasipca

import androidx.compose.runtime.*
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.Notification
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sasipca.auth.MicrosoftAuthManagerDesktop
import sasipca.composeapp.generated.resources.Res
import sasipca.composeapp.generated.resources.icon512x512
import sasipca.network.ApiClient
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.utils.ObserveScreenSize
import sasipca.utils.SignalRManager
import java.util.prefs.Preferences

fun main() = application {
    // 1. Inicializações
    val desktopSettings: Settings = PreferencesSettings(Preferences.userRoot().node("sasipca"))

    SessionManager.init(desktopSettings)
    SettingsManager.init(desktopSettings)

    val msAuthManager = remember { MicrosoftAuthManagerDesktop() }
    ApiClient.init(msAuthManager)

    // 2. UI State
    var isWindowVisible by remember { mutableStateOf(true) }
    val trayState = rememberTrayState()
    val logoPainter = painterResource(Res.drawable.icon512x512)

    // 3. SignalR Manager
    val signalR = remember {
        SignalRManager(
            onNotificationReceived = { title, msg ->
                trayState.sendNotification(
                    Notification(title, msg, Notification.Type.Info)
                )
            }
        )
    }

    // 4. Lógica Reativa: Observar Login para arrancar/parar SignalR
    // Convertemos o StateFlow do SessionManager para um State do Compose
    val isLoggedIn by SessionManager.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Se o utilizador entrou (ou abriu a app já logado), arranca o serviço.
            // O SignalRManager.start() deve ter o loop de retry que fizemos antes.
            launch(Dispatchers.IO) {
                signalR.start()
            }
        } else {
            // Se o utilizador fez logout, desliga o serviço.
            signalR.stop()
        }
    }

    // 5. Tray e Janela (Mantêm-se iguais)
    Tray(
        state = trayState,
        icon = logoPainter,
        tooltip = "SASIPCA",
        onAction = { isWindowVisible = true },
        menu = {
            Item("Abrir", onClick = { isWindowVisible = true })
            Separator()
            Item("Sair", onClick = {
                signalR.stop()
                exitApplication()
            })
        }
    )

    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                isWindowVisible = false
                trayState.sendNotification(
                    Notification("SASIPCA", "A aplicação continua a correr em segundo plano.", Notification.Type.Info)
                )
            },
            title = "Serviços de Ação Social IPCA",
            icon = logoPainter
        ) {
            ObserveScreenSize(window)
            App()
        }
    }
}