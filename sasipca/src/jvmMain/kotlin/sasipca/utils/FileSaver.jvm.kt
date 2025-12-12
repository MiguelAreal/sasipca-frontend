package sasipca.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

actual class PlatformFileSaver actual constructor() : FileSaver {

    override suspend fun saveFile(fileName: String, data: ByteArray, openFile: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Guardar o ficheiro
                val userHome = System.getProperty("user.home")
                val file = File(userHome, "Downloads/$fileName")

                // Garante que a pasta existe
                file.parentFile?.mkdirs()

                // Escreve os dados
                file.writeBytes(data)
                println("Ficheiro guardado em: ${file.absolutePath}")

                // 2. Abrir o ficheiro (se solicitado)
                if (openFile) {
                    if (Desktop.isDesktopSupported()) {
                        val desktop = Desktop.getDesktop()

                        if (desktop.isSupported(Desktop.Action.OPEN)) {
                            // Esta linha lança o processo para abrir o PDF/CSV
                            desktop.open(file)
                        } else {
                            println("A ação 'OPEN' não é suportada neste sistema operativo.")
                        }
                    } else {
                        println("A API Desktop não é suportada neste ambiente.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}