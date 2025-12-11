package sasipca

import androidx.compose.runtime.*
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
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
    // 1. Inicializações de Sistema
    val desktopSettings: Settings = PreferencesSettings(Preferences.userRoot().node("sasipca"))
    SessionManager.init(desktopSettings)
    SettingsManager.init(desktopSettings)

    // 2. Inicializações de Autenticação e API
    val msAuthManager = remember { MicrosoftAuthManagerDesktop() }
    ApiClient.init(msAuthManager)

    // 3. Gestão de Janela e Tray (Background)
    var isWindowVisible by remember { mutableStateOf(true) }
    val trayState = rememberTrayState()
    val iconPainter = painterResource(Res.drawable.icon512x512)

    // 4. SignalR (Notificações)
    val signalR = remember { SignalRManager() }
    val scope = rememberCoroutineScope()

    // Arranca o SignalR se estiver logado (e mantém vivo mesmo se a janela fechar)
    LaunchedEffect(Unit) {
        if (SessionManager.isLoggedInNow()) {
            scope.launch(Dispatchers.IO) {
                signalR.start()
            }
        }
    }

    // 5. System Tray (Ícone ao pé do relógio)
    Tray(
        state = trayState,
        icon = iconPainter,
        tooltip = "SASIPCA",
        onAction = { isWindowVisible = true }, // Clique no ícone abre a janela
        menu = {
            Item("Abrir", onClick = { isWindowVisible = true })
            Separator()
            Item("Sair", onClick = {
                signalR.stop()
                exitApplication() // Encerra totalmente a app
            })
        }
    )

    // 6. Janela Principal
    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                // Em vez de fechar a app, esconde a janela
                isWindowVisible = false

                // Envia notificação de sistema a avisar
                trayState.sendNotification(
                    androidx.compose.ui.window.Notification(
                        "SASIPCA",
                        "A aplicação continua a correr em segundo plano."
                    )
                )
            },
            title = "Serviços de Ação Social IPCA",
            icon = iconPainter
        ) {
            ObserveScreenSize(window)
            App()
        }
    }
}