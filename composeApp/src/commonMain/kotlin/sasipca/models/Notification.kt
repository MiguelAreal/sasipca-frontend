package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val date: String, // O backend envia DateTime como string ISO
    val isRead: Boolean
)