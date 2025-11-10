package sasipca.models

import kotlinx.serialization.Serializable


/**
 * Representa dados a introduzir para criar ou editar um beneficiário
 */
@Serializable
data class BeneficiaryPostDTO(
    val name: String,
    val email: String,
    val contact: String,
    val course: String?,
    val curricularYear: Int?,
    val studentNum: Int?,
    val nif: Int?,
    val globalObs: String?,
    val particularObs: String?,
    val street: String?,
    val number: Int?,
    val postalCode: String?
)


/**
 * Representa um beneficiário dentro de uma lista de beneficiários (mais simples)
 */
@Serializable
data class BeneficiaryListDTO(
    val beneficiaryId: Int,
    val name: String,
    val email: String? = null
)


/**
 * Representa um beneficiário devolvido pela API
 */
@Serializable
data class BeneficiaryGetDTO(
    val beneficiaryId: Int,
    val name: String,
    val email: String,
    val contact: String,
    val course: String?,
    val curricularYear: Int?,
    val studentNum: Int?,
    val nif: Int?,
    val globalObs: String?,
    val particularObs: String?,
    val street: String?,
    val number: Int?,
    val postalCode: String?
)
