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

