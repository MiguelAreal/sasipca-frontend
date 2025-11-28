package sasipca.utils

interface FileSaver {
    suspend fun saveFile(fileName: String, data: ByteArray, openFile: Boolean = false)
}

expect class PlatformFileSaver() : FileSaver