package g8.ipca.sasipca.sasipca

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.*
import g8.ipca.sasipca.sasipca.storage.SessionManager
import java.util.prefs.Preferences

fun main() = application {
    // Cria um objeto PreferencesSettings
    val desktopSettings: Settings = PreferencesSettings(Preferences.userRoot().node("sasipca"))

    SessionManager.init(desktopSettings)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Serviços de Ação Social IPCA",
    ) {
        App()
    }
}