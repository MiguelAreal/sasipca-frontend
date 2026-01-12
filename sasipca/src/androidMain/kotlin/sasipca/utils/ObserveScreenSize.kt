package sasipca.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import sasipca.storage.ScreenSizeManager

@Composable
fun ObserveScreenSize() {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    // Obtemos o tamanho em píxeis
    val sizePx = windowInfo.containerSize

    // CONVERSÃO CORRETA: Pixels / Density = DP
    val widthDp = with(density) { sizePx.width.toDp() }
    val heightDp = with(density) { sizePx.height.toDp() }

    LaunchedEffect(widthDp, heightDp) {
        ScreenSizeManager.updateSize(widthDp, heightDp)
    }
}