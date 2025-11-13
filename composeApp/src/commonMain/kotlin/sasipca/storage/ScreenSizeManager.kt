package sasipca.storage

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ScreenSizeManager {
    private val _screenWidth = MutableStateFlow(0.dp)
    private val _screenHeight = MutableStateFlow(0.dp)

    val screenWidth: StateFlow<Dp> get() = _screenWidth
    val screenHeight: StateFlow<Dp> get() = _screenHeight

    fun updateSize(widthDp: Dp, heightDp: Dp) {
        _screenWidth.value = widthDp
        _screenHeight.value = heightDp
    }

    /** Returns true if the screen width is considered small. */
    fun isSmallScreen(): Boolean = _screenWidth.value < 800.dp

    /** Returns true if the screen width is considered large. */
    fun isLargeScreen(): Boolean = _screenWidth.value >= 800.dp
}
