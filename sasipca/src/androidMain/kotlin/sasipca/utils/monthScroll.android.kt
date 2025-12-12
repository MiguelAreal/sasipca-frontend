package sasipca.utils

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

actual fun Modifier.monthScroll(
    listState: LazyListState,
    coroutineScope: CoroutineScope
): Modifier = pointerInput(Unit) {
    var dragOffset = 0f
    detectVerticalDragGestures(
        onVerticalDrag = { _, dragAmount ->
            dragOffset += dragAmount
        },
        onDragEnd = {
            val threshold = 80f // ajusta sensibilidade
            if (dragOffset > threshold) {
                coroutineScope.launch {
                    val targetIndex = (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                    listState.animateScrollToItem(targetIndex)
                }
            } else if (dragOffset < -threshold) {
                coroutineScope.launch {
                    val targetIndex = (listState.firstVisibleItemIndex + 1)
                    listState.animateScrollToItem(targetIndex)
                }
            }
            dragOffset = 0f
        }
    )
}
