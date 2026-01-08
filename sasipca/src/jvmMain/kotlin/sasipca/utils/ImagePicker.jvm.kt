package sasipca.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerLauncher {
    val scope = rememberCoroutineScope()

    return remember {
        ImagePickerLauncher {
            scope.launch {
                // Executar na thread do Swing/AWT para não bloquear a UI do Compose
                val fileDialog = FileDialog(null as Frame?, "Selecionar Imagem", FileDialog.LOAD)
                fileDialog.file = "*.jpg;*.jpeg;*.png"
                fileDialog.isVisible = true

                if (fileDialog.file != null) {
                    val file = File(fileDialog.directory, fileDialog.file)
                    onImagePicked(file.readBytes())
                } else {
                    onImagePicked(null) // Cancelado
                }
            }
        }
    }
}