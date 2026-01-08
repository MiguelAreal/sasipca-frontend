package sasipca.utils


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import sasipca.storage.ScreenSizeManager

@Composable
fun ObserveScreenSize() {
    val config = LocalWindowInfo.current.containerSize
    LocalDensity.current

    // Convert dp to pixels
    val widthDp = config.width.dp
    val heightDp = config.height.dp

    // Update the ScreenSizeManager whenever size changes
    LaunchedEffect(widthDp, heightDp) {
        ScreenSizeManager.updateSize(widthDp, heightDp)
    }
}
