package g8.ipca.sasipca.sasipca.models

import kotlinx.serialization.Serializable

/**
 * Modelo genérico que contém uma mensagem vinda da API
 */
@Serializable
data class Resposta(
    val message: String
)
