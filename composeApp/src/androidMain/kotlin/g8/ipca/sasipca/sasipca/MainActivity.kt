package g8.ipca.sasipca.sasipca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import g8.ipca.sasipca.sasipca.storage.SessionManager
import g8.ipca.sasipca.sasipca.storage.SettingsManager
import g8.ipca.sasipca.sasipca.ui.theme.IPCAGreen
import g8.ipca.sasipca.sasipca.ui.theme.SasIpcaTheme
import java.util.prefs.Preferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa sessão
        val settings: Settings = SharedPreferencesSettings(androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
        SettingsManager.init(settings)
        SessionManager.init(settings)

        setContent {
            SasIpcaTheme {
                SideEffect {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(IPCAGreen.toArgb()), // verde + ícones brancos
                        navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()) // transparente
                    )
                }

                App()
            }
        }
    }
}
