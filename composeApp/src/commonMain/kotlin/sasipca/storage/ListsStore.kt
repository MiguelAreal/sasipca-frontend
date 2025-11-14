package sasipca.storage

import sasipca.models.CategoryDTO
import sasipca.models.UnitTypeDTO

object ListsStore {

    var categories: List<CategoryDTO> = emptyList()
        private set

    var unitTypes: List<UnitTypeDTO> = emptyList()
        private set

    fun load(categories: List<CategoryDTO>, unitTypes: List<UnitTypeDTO>) {
        this.categories = categories
        this.unitTypes = unitTypes
    }

    fun getCategoryName(id: Int): String =
        categories.firstOrNull { it.id == id }?.type ?: "Desconhecido"

    fun getUnitTypeName(id: Int): String =
        unitTypes.firstOrNull { it.id == id }?.type ?: "Desconhecido"

    fun getCategoryId(type: String): Int? =
        categories.firstOrNull { it.type == type }?.id

    fun getUnitTypeId(type: String): Int? =
        unitTypes.firstOrNull { it.type == type }?.id
}
