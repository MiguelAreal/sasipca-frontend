package sasipca.utils

/**
 * ‘Interface’ que define funções de guardar dados.
 */
interface FileSaver {
    suspend fun saveFile(fileName: String, data: ByteArray, openFile: Boolean = false)
}

/**
 * Implementação multiplataforma para guardar dados.
 */
expect class PlatformFileSaver() : FileSaver