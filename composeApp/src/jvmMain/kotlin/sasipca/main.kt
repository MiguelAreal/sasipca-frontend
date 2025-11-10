package sasipca

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.*
import io.ktor.client.HttpClient
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import org.jetbrains.compose.resources.painterResource
import sasipca.composeapp.generated.resources.Res
import sasipca.composeapp.generated.resources.icon512x512
import java.util.prefs.Preferences

fun main() = application {
    // Configuração de settings locais
    val desktopSettings: Settings = PreferencesSettings(Preferences.userRoot().node("sasipca"))
    SessionManager.init(desktopSettings)
    SettingsManager.init(desktopSettings)

    ApiClient.init()

    // Load all icon sizes
    val iconPainter = painterResource(Res.drawable.icon512x512)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Serviços de Ação Social IPCA",
        icon = iconPainter
    ) {
        App()
    }
}
