package g8.ipca.sasipca.sasipca.datamodels
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginSuccessResponse(
    val userName: String,
    val userID: Int,
    val token: String
)

@Serializable
data class LoginErrorResponse(
    val message: String
)
