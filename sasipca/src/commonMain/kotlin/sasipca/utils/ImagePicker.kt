package sasipca.utils

import androidx.compose.runtime.Composable

expect class ImagePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerLauncher