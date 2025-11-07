package sasipca.models

import sasipca.ui.components.NamedItem

data class Category(
    val id: Int,
    override val name: String
) : NamedItem

data class UnitType(
    val id: Int,
    override val name: String
) : NamedItem

data class LotToEnter(
    var lotNumber: String,
    var expirationDate: String,
    var quantity: String
)