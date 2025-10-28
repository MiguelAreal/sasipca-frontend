package g8.ipca.sasipca.sasipca

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.*
import g8.ipca.sasipca.sasipca.storage.SessionManager

fun main() = application {
    /*val desktopSettings = JvmSettings("sasipca_prefs.properties")
    SessionManager.init(desktopSettings)*/

    Window(
        onCloseRequest = ::exitApplication,
        title = "Serviços de Ação Social IPCA",
    ) {
        App()
    }
}