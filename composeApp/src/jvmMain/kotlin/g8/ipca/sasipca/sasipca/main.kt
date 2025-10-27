package g8.ipca.sasipca.sasipca

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Serviços de Ação Social IPCA",
    ) {
        App()
    }
}