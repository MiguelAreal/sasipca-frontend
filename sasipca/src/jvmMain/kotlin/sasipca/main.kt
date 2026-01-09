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
import sasipca.network.ApiClient
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.utils.ObserveScreenSize
import sasipca.utils.SignalRManager
import sasipca_app.sasipca.generated.resources.Res
import sasipca_app.sasipca.generated.resources.icon512x512
import java.util.prefs.Preferences

/**
 * Envia notificações nativas. No Linux, usa o notify-send para integração perfeita com o KDE.
 */
fun sendSmartNotification(trayState: androidx.compose.ui.window.TrayState, title: String, msg: String) {
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("linux")) {
        try {
            ProcessBuilder("notify-send", "-a", "SASIPCA", "-i", "dialog-information", title, msg).start()
        } catch (e: Exception) {
            trayState.sendNotification(Notification(title, msg, Notification.Type.Info))
        }
    } else {
        trayState.sendNotification(Notification(title, msg, Notification.Type.Info))
    }
}

fun main() {
    val os = System.getProperty("os.name").lowercase()

    if (os.contains("linux")) {
        // ESSENCIAL: Força o backend X11 no Linux para que o Tray Menu funcione via XWayland.
        // O Wayland nativo muitas vezes bloqueia popups de aplicações AWT/Swing (Java).
        System.setProperty("jdk.gtk.version", "3")
        System.setProperty("compose.interop.blending.enabled", "true")

        // Esta linha ajuda o KDE a mapear o menu de contexto corretamente
        System.setProperty("java.awt.headless", "false")
    }

    application {
        // 1. Inicializações e Persistência
        val desktopSettings: Settings = remember {
            PreferencesSettings(Preferences.userRoot().node("sasipca"))
        }

        LaunchedEffect(Unit) {
            SessionManager.init(desktopSettings)
            SettingsManager.init(desktopSettings)
        }

        val msAuthManager = remember { MicrosoftAuthManagerDesktop() }
        remember { ApiClient.init(msAuthManager) }

        // 2. Estado da UI
        var isWindowVisible by remember { mutableStateOf(true) }
        val trayState = rememberTrayState()

        // Nota: O ícone de 512px será redimensionado automaticamente,
        // mas se o menu falhar, tente usar um recurso de 32x32 apenas para o tray.
        val logoPainter = painterResource(Res.drawable.icon512x512)

        // 3. Gestão de SignalR
        val signalR = remember {
            SignalRManager(
                onNotificationReceived = { title, msg ->
                    sendSmartNotification(trayState, title, msg)
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

        // 5. Janela Principal
        if (isWindowVisible) {
            Window(
                onCloseRequest = {
                    isWindowVisible = false
                    sendSmartNotification(
                        trayState,
                        "SASIPCA",
                        "A aplicação continua a correr na barra de tarefas."
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
}