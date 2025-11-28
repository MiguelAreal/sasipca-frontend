package sasipca

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.launch
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.theme.SasIpcaTheme
import sasipca.utils.ObserveScreenSize
import sasipca.auth.MicrosoftAuthManagerAndroid
class MainActivity : ComponentActivity() {

    // Guarda referência para gerir a currentActivity
    private lateinit var msAuthManager: MicrosoftAuthManagerAndroid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sasipca.utils.AndroidContext.set(this)

        val settings: Settings = SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(this))
        SessionManager.init(settings)
        SettingsManager.init(settings)

        msAuthManager = MicrosoftAuthManagerAndroid(this)
        lifecycleScope.launch {
            msAuthManager.init()
        }

        ApiClient.init(msAuthManager)

        setContent {
            msAuthManager.currentActivity = this

            SasIpcaTheme {
                ObserveScreenSize()
                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::msAuthManager.isInitialized) {
            msAuthManager.currentActivity = null
        }
    }
}
