package sasipca

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kdroid.composetray.tray.impl.LinuxTrayInitializer
import com.kdroid.composetray.tray.impl.WindowsTrayInitializer
import com.russhwolf.settings.PreferencesSettings
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
import sasipca_app.sasipca.generated.resources.icon16x16
import java.awt.*
import java.io.File
import java.util.prefs.Preferences
import kotlin.system.exitProcess
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Notificações nativas.
 */
fun sendSmartNotification(title: String, msg: String) {
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("linux")) {
        try {
            ProcessBuilder("notify-send", "-a", "SASIPCA", title, msg).start()
        } catch (_: Exception) {}
    } else if (os.contains("windows")) {
        try {
            if (SystemTray.isSupported()) {
                val tray = SystemTray.getSystemTray()
                val image = Toolkit.getDefaultToolkit().createImage(ByteArray(0))
                val trayIcon = TrayIcon(image, "SASIPCA")
                tray.add(trayIcon)
                trayIcon.displayMessage(title, msg, TrayIcon.MessageType.INFO)
                tray.remove(trayIcon)
            }
        } catch (e: Exception) {
            println("[ERROR] Notificação falhou: ${e.message}")
        }
    }
}

/**
 * Extração robusta com verificação de integridade.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun getTrayIconPath(): String {
    val os = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")
    // Pasta sem ponto inicial para evitar que o Windows oculte ou bloqueie o acesso
    val storageDir = File(userHome, "SASIPCA_Assets").apply { if (!exists()) mkdirs() }

    // Lógica de seleção de ficheiro
    val (resourceName, fileName) = if (os.contains("windows")) {
        "drawable/icon16x16.ico" to "tray_icon.ico"
    } else {
        "drawable/icon32x32.png" to "tray_icon.png"
    }

    val iconFile = File(storageDir, fileName)

    return try {
        val bytes = Res.readBytes(resourceName)
        iconFile.writeBytes(bytes)

        // canonicalPath resolve qualquer ambiguidade de caminho no Windows
        val path = iconFile.canonicalPath
        println("[DEBUG] Ícone extraído com sucesso: $path (${iconFile.length()} bytes)")
        path
    } catch (e: Exception) {
        println("[ERROR] Falha ao extrair $resourceName: ${e.message}")
        ""
    }
}

var isWindowVisibleGlobal by mutableStateOf(true)

fun main() {
    val os = System.getProperty("os.name").lowercase()

    if (os.contains("linux")) {
        System.setProperty("jdk.gtk.version", "3")
        System.setProperty("java.awt.headless", "false")
    }

    val prefs = Preferences.userRoot().node("sasipca")
    val desktopSettings = PreferencesSettings(prefs)

    // Inicializar managers de forma global/estática antes da UI
    SessionManager.init(desktopSettings)
    SettingsManager.init(desktopSettings)

    application {
        val windowIcon = painterResource(Res.drawable.icon16x16)
        var trayPath by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            trayPath = getTrayIconPath()
        }

        val msAuthManager = remember { MicrosoftAuthManagerDesktop() }
        remember { ApiClient.init(msAuthManager) }
        val signalR = remember { SignalRManager { t, m -> sendSmartNotification(t, m) } }

        val isLoggedIn by SessionManager.isLoggedIn.collectAsState()
        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) launch(Dispatchers.IO) { signalR.start() } else signalR.stop()
        }

        // --- INICIALIZAÇÃO DO TRAY ---
        if (trayPath.isNotEmpty()) {
            DisposableEffect(trayPath) {
                val tooltip = "SASIPCA"
                try {
                    if (os.contains("linux")) {
                        LinuxTrayInitializer.initialize(
                            iconPath = trayPath,
                            tooltip = tooltip,
                            onLeftClick = { isWindowVisibleGlobal = true },
                            menuContent = {
                                Item("Abrir") { isWindowVisibleGlobal = true }
                                Divider()
                                Item("Sair") { exitProcess(0) }
                            }
                        )
                    } else if (os.contains("windows")) {
                        WindowsTrayInitializer.initialize(
                            iconPath = trayPath,
                            tooltip = tooltip,
                            onLeftClick = { isWindowVisibleGlobal = true },
                            menuContent = {
                                Item("Abrir") { isWindowVisibleGlobal = true }
                                Divider()
                                Item("Sair") { exitProcess(0) }
                            }
                        )
                    }
                } catch (e: Exception) {
                    println("[ERROR] Erro ao carregar ícone no Tray: ${e.message}")
                }

                onDispose {
                    if (os.contains("linux")) LinuxTrayInitializer.dispose()
                    else if (os.contains("windows")) WindowsTrayInitializer.dispose()
                }
            }
        }

        if (isWindowVisibleGlobal) {
            Window(
                onCloseRequest = {
                    isWindowVisibleGlobal = false
                    sendSmartNotification("SASIPCA", "A correr em segundo plano.")
                },
                title = "Serviços de Ação Social IPCA",
                icon = windowIcon
            ) {
                ObserveScreenSize(window)
                App()
            }
        }
    }
}