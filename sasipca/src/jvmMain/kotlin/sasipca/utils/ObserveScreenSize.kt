package sasipca.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.storage.ScreenSizeManager
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

@Composable
fun ObserveScreenSize(window: Window) {
    val density = LocalDensity.current

    // Set initial size
    LaunchedEffect(Unit) {
        val widthDp: Dp
        val heightDp: Dp
        with(density) {
            widthDp = window.width.toDp()
            heightDp = window.height.toDp()
        }
        ScreenSizeManager.updateSize(widthDp, heightDp)
    }

    // Listen for resizes
    LaunchedEffect(window) {
        val scope = CoroutineScope(Dispatchers.Default)
        window.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                scope.launch {
                    val widthDp: Dp
                    val heightDp: Dp
                    with(density) {
                        widthDp = window.width.toDp()
                        heightDp = window.height.toDp()
                    }
                    ScreenSizeManager.updateSize(widthDp, heightDp)
                }
            }
        })
    }
}
