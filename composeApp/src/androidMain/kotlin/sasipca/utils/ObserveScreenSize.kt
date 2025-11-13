package sasipca.utils


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import sasipca.storage.ScreenSizeManager

@Composable
fun ObserveScreenSize() {
    val config = LocalConfiguration.current
    val density = LocalDensity.current

    // Convert dp to pixels
    val widthDp = config.screenWidthDp.dp
    val heightDp = config.screenHeightDp.dp

    // Update the ScreenSizeManager whenever size changes
    LaunchedEffect(widthDp, heightDp) {
        ScreenSizeManager.updateSize(widthDp, heightDp)
    }
}
