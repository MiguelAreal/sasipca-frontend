package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class PostAdmin(
    val email: String,
    val contact: String
)

@Serializable
data class AdminUser(
    val id: Int,
    val name: String,
    val email: String,
    val contact: String
)