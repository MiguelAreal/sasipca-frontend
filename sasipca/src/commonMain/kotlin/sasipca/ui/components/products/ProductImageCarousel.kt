package sasipca.ui.components.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun ProductImagesCarousel(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    var showPopup by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .pointerInput(images) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount < -10 && currentIndex < images.lastIndex) {
                        currentIndex++
                    } else if (dragAmount > 10 && currentIndex > 0) {
                        currentIndex--
                    }
                }
            }
            .clickable { showPopup = true }
    ) {
        if (images.isNotEmpty()) {
            AsyncImage(
                model = images[currentIndex],
                contentDescription = "Imagem do produto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }

    if (showPopup) {
        ImageCarouselPopup(
            images = images,
            onDismiss = { showPopup = false }
        )
    }
}
