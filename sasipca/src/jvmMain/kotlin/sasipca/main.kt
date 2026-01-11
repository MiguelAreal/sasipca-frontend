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
import sasipca_app.sasipca.generated.resources.icon512x512
import java.io.File
import java.util.prefs.Preferences
import kotlin.system.exitProcess

/**
 * Notificações via sistema.
 */
fun sendSmartNotification(title: String, msg: String) {
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("linux")) {
        try {
            ProcessBuilder("notify-send", "-a", "SASIPCA", title, msg).start()
        } catch (_: Exception) {}
    }
}
/**
 * Extrai o ícone dos recursos para um ficheiro temporário.
 */
fun getTrayIconPath(): String {
    val tempDir = System.getProperty("java.io.tmpdir")
    val iconFile = File(tempDir, "sasipca_tray_icon.png")

    try {
        // Tenta os caminhos comuns de recursos processados pelo Compose Multiplatform
        val resourcePaths = listOf(
            "composeResources/sasipca_app.sasipca.generated.resources/drawable/icon32x32.png",
            "drawable/icon32x32.png"
        )

        var inputStream: java.io.InputStream? = null
        for (path in resourcePaths) {
            inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
            if (inputStream != null) break
        }

        if (inputStream != null) {
            inputStream.use { input ->
                iconFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            println("[DEBUG] Ícone extraído com sucesso para: ${iconFile.absolutePath} (${iconFile.length()} bytes)")
        } else {
            println("[ERROR] Não foi possível encontrar o recurso do ícone nos ClassLoaders.")
        }
    } catch (e: Exception) {
        println("[ERROR] Falha ao extrair ícone: ${e.message}")
    }

    return iconFile.absolutePath
}

var isWindowVisibleGlobal by mutableStateOf(true)

fun main() {
    val os = System.getProperty("os.name").lowercase()

    if (os.contains("linux")) {
        System.setProperty("jdk.gtk.version", "3")
        System.setProperty("java.awt.headless", "false")
    }

    androidx.compose.ui.window.application {
        val desktopSettings = remember { PreferencesSettings(Preferences.userRoot().node("sasipca")) }
        val windowIcon = painterResource(Res.drawable.icon512x512)

        LaunchedEffect(Unit) {
            SessionManager.init(desktopSettings)
            SettingsManager.init(desktopSettings)
        }

        val msAuthManager = remember { MicrosoftAuthManagerDesktop() }
        remember { ApiClient.init(msAuthManager) }
        val signalR = remember { SignalRManager { t, m -> sendSmartNotification(t, m) } }

        val isLoggedIn by SessionManager.isLoggedIn.collectAsState()
        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) launch(Dispatchers.IO) { signalR.start() } else signalR.stop()
        }

        // --- INICIALIZAÇÃO KDROID COM CAMINHO DINÂMICO ---
        DisposableEffect(Unit) {
            val iconPath = getTrayIconPath()
            val trayTooltip = "SASIPCA"

            if (os.contains("linux")) {
                LinuxTrayInitializer.initialize(
                    iconPath = iconPath,
                    tooltip = trayTooltip,
                    onLeftClick = { isWindowVisibleGlobal = true },
                    menuContent = {
                        Item("Abrir") { isWindowVisibleGlobal = true }
                        Divider()
                        Item("Sair") { exitProcess(0) }
                    }
                )
            } else if (os.contains("windows")) {
                WindowsTrayInitializer.initialize(
                    iconPath = iconPath,
                    tooltip = trayTooltip,
                    onLeftClick = { isWindowVisibleGlobal = true },
                    menuContent = {
                        Item("Abrir") { isWindowVisibleGlobal = true }
                        Divider()
                        Item("Sair") { exitProcess(0) }
                    }
                )
            }
            onDispose {
                if (os.contains("linux")) LinuxTrayInitializer.dispose()
                else if (os.contains("windows")) WindowsTrayInitializer.dispose()
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