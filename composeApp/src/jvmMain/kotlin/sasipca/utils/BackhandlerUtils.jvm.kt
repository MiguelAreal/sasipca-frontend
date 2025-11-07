package sasipca.utils

import androidx.compose.runtime.Composable

@Composable
actual fun SafeBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No desktop não há botão físico, por isso não faz nada
}
