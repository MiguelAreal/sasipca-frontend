package g8.ipca.sasipca.sasipca.datamodels
import kotlinx.serialization.Serializable

@Serializable
data class LotToEnter(
    val lot: String,
    val quantity: String,
    val expiryDate: String
)