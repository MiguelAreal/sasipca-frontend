package sasipca.models

import kotlinx.serialization.Serializable

@Serializable
data class Lists(
    val categories: List<CategoryDTO>,
    val types: List<UnitTypeDTO>
)

@Serializable
data class CategoryDTO(
    val id: Int,
    val type: String
)
@Serializable
data class UnitTypeDTO(
    val id: Int,
    val type: String
)
