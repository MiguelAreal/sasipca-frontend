package g8.ipca.sasipca.sasipca.models

data class LotToEnter(
    val lot: String,
    val quantity: String,
    val expiryDate: String
)

data class Category(
    val id: Int,
    val name: String
)

data class UnitType(
    val id: Int,
    val name: String
)
