package sasipca.network

import kotlinx.serialization.Serializable

// Modelo para ler o JSON de erro do backend
@Serializable
data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null
)

// Exceção personalizada para a UI apanhar
class SasipcaApiException(
    override val message: String
) : Exception(message)