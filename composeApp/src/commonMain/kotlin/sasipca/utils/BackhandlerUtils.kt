package sasipca.utils

import androidx.compose.runtime.Composable

@Composable
expect fun SafeBackHandler(enabled: Boolean = true, onBack: () -> Unit)
