package g8.ipca.sasipca.sasipca.utils

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SnackbarManager {
    lateinit var snackbarState: MutableState<SnackbarMessage?>
    lateinit var scope: CoroutineScope

    fun show(message: String, type: SnackbarType = SnackbarType.SUCCESS) {
        if (::snackbarState.isInitialized && ::scope.isInitialized) {
            scope.launch {
                // Set message
                snackbarState.value = SnackbarMessage(message, type)
                // Keep it visible for a while
                delay(3000)
                // Clear message to trigger exit animation
                snackbarState.value = null
            }
        }
    }
}
