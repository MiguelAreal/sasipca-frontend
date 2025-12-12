package sasipca.models

import kotlinx.serialization.Serializable

/**
 * Modelo de leitura (GET)
 */
@Serializable
data class Campaign(
    val id: Int,
    val name: String,
    val description: String?,
    val location: String?,
    val imageUrl: String?,
    val startDate: String, // Formato "yyyy-MM-dd"
    val endDate: String,   // Formato "yyyy-MM-dd"
    val creatorName: String
)