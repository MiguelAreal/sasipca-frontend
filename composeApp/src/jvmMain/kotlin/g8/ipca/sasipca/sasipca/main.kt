package g8.ipca.sasipca.sasipca

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.*
import g8.ipca.sasipca.sasipca.storage.SessionManager
import org.jetbrains.compose.resources.painterResource
import sasipca.composeapp.generated.resources.Res
import sasipca.composeapp.generated.resources.icon512x512
import sasipca.composeapp.generated.resources.logo
import java.awt.image.BufferedImage
import java.util.prefs.Preferences
import javax.imageio.ImageIO

fun main() = application {
    val desktopSettings: Settings = PreferencesSettings(Preferences.userRoot().node("sasipca"))

    SessionManager.init(desktopSettings)

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