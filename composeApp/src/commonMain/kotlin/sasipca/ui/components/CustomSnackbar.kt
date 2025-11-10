package sasipca.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sasipca.utils.SnackbarMessage
import sasipca.utils.SnackbarType

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomSnackbarHost(
    snackbarMessageState: MutableState<SnackbarMessage?>,
    modifier: Modifier = Modifier
) {
    // Remember the last message while visible
    val currentMessage = remember { mutableStateOf<SnackbarMessage?>(null) }

    // Update currentMessage only when a new message appears
    val msg = snackbarMessageState.value
    if (msg != null) {
        currentMessage.value = msg
    }

    AnimatedVisibility(
        visible = msg != null, // true while snackbarState has a message
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(tween(300)),
        exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut(tween(300))
    ) {
        val message = currentMessage.value ?: return@AnimatedVisibility

        val color = when (message.type) {
            SnackbarType.SUCCESS -> Color(0xFF4CAF50)
            SnackbarType.ERROR -> Color(0xFFF44336)
            SnackbarType.WARNING -> Color(0xFFFFC107)
        }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.TopEnd
        ) {
            Snackbar(
                containerColor = color,
                contentColor = Color.White,
                actionContentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(message.message)
            }
        }
    }
}
