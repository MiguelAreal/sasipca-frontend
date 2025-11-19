package sasipca.models

import kotlinx.serialization.Serializable


/**
 * Representa dados a introduzir para criar ou editar um beneficiário
 */
@Serializable
data class BeneficiaryPost(
    val name: String,
    val email: String,
    var contact: String,
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
 * Representa um beneficiário numa lista de beneficiários (mais simples)
 */
@Serializable
data class BeneficiaryItem(
    val beneficiaryId: Int,
    val name: String,
    val email: String? = null
)


/**
 * Representa um beneficiário devolvido pela API
 */
@Serializable
data class BeneficiaryGet(
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
