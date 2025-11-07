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
    val course: String,
    val curricularYear: Int,
    val globalObs: String? = null,
    val particularObs: String? = null,
    val street: String? = null,
    val number: Int? = null,
    val postalCode: String? = null
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
    val course: String,
    val curricularYear: Int? = null,
    val studentNum: Int? = null,
    val nif: Int? = null
)
