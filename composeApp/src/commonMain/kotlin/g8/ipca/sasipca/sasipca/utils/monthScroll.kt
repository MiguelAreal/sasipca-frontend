package g8.ipca.sasipca.sasipca.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

expect fun Modifier.monthScroll(
    listState: LazyListState,
    coroutineScope: CoroutineScope
): Modifier

