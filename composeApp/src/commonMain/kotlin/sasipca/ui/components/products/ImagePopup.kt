package sasipca.ui.components.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage

@Composable
fun ImagePopup(
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    if (imageUrl != null) {
        Dialog(onDismissRequest = onDismiss) {
            // The Box ensures the image is centered, nothing else is drawn
            Box(
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagem do produto (ampliada)",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.5f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
