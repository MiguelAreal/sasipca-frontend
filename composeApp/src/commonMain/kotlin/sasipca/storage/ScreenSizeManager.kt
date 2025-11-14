package sasipca.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ScreenSizeManager {
    private val _screenWidth = MutableStateFlow(0.dp)
    val screenWidth: StateFlow<Dp> get() = _screenWidth
    private val _screenHeight = MutableStateFlow(0.dp)
    val screenHeight: StateFlow<Dp> get() = _screenHeight

    fun updateSize(widthDp: Dp, heightDp: Dp) {
        _screenWidth.value = widthDp
        _screenHeight.value = heightDp
    }

    @Composable
    fun isLargeScreen(): Boolean {
        val width by screenWidth.collectAsState()
        return width >= 800.dp
    }

    @Composable
    fun isSmallScreen(): Boolean {
        val width by screenWidth.collectAsState()
        return width < 800.dp
    }
}
