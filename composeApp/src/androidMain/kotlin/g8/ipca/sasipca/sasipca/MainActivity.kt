package g8.ipca.sasipca.sasipca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.preference.PreferenceManager
import com.russhwolf.settings.AndroidSettings
import g8.ipca.sasipca.sasipca.storage.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializa SessionManager com Settings Android
        val androidSettings = AndroidSettings(
            PreferenceManager.getDefaultSharedPreferences(this)
        )

        SessionManager.init(androidSettings)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}