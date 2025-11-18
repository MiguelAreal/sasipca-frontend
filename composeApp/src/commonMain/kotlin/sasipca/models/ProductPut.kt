package sasipca.models

import kotlinx.serialization.Serializable

/**
 * Classe que serve para atualizar o cabeçalho de um produto.
 * Só são aplicados os valores que forem na classe, o que não tiver não se mexe
 */
@Serializable
data class ProductPut(
    val name: String ? = null,
    val unitSize: Int? = null,
    val categoryId: Int? = null,
    val unitId: Int? = null,
)
