package sasipca.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.monthScroll(
    listState: LazyListState,
    coroutineScope: CoroutineScope
): Modifier = composed {
    this.onPointerEvent(PointerEventType.Scroll) { event ->
        val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f

        // Se o delta for positivo, subimos uma semana, se negativo, descemos
        if (delta.absoluteValue > 2) { // ignora micro scrolls
            val direction = if (delta > 0) -1 else 1
            coroutineScope.launch {
                // Sempre usa firstVisibleItemIndex + direction, mas snap para ‘item’
                val targetIndex = (listState.firstVisibleItemIndex + direction)
                    .coerceIn(0, listState.layoutInfo.totalItemsCount - 1)
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
}
