package g8.ipca.sasipca.sasipca.utils

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun SafeBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val activity = LocalContext.current as? Activity

    BackHandler(enabled = enabled) {
        if (enabled) {
            onBack()
        } else {
            // fallback: se não houver overlay,terminar a activity
            activity?.finish()
        }
    }
}
