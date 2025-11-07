package sasipca.models

import kotlinx.serialization.Serializable


@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)
