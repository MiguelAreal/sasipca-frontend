package sasipca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.theme.IPCAGreen
import sasipca.ui.theme.SasIpcaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa sessão
        val settings: Settings = SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(this))
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
