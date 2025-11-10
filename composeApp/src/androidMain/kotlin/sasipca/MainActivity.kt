package sasipca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.theme.SasIpcaTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings: Settings = SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(this))
        SessionManager.init(settings)
        SettingsManager.init(settings)

        ApiClient.init()

        setContent {
            SasIpcaTheme {
                App()
            }
        }
    }
}
