package sasipca.utils
import android.content.Intent
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class PlatformFileSaver actual constructor(): FileSaver {

    override suspend fun saveFile(fileName: String, data: ByteArray, openFile: Boolean) {
        withContext(Dispatchers.IO) {
            val context = AndroidContext.get() ?: return@withContext

            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, fileName)

            FileOutputStream(file).use { it.write(data) }

            if (openFile) {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    // Determinar MIME type (PDF ou CSV)
                    val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Necessário se o contexto não for Activity
                    }

                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    SnackbarManager.show("Não foi possível abrir o ficheiro",SnackbarType.ERROR)
                }
            }
        }
    }
}