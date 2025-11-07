package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.models.BeneficiaryGetDTO
import sasipca.models.BeneficiaryPostDTO
import sasipca.models.Resposta
import sasipca.repositories.BeneficiaryRepository
import sasipca.utils.SnackbarManager
import sasipca.utils.SnackbarType

/**
 * ViewModel responsável por gerir o estado de um beneficiário específico.
 */
class BeneficiaryDetailViewModel(
    private val repository: BeneficiaryRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var beneficiary by mutableStateOf<BeneficiaryGetDTO?>(null)
        private set

    /**
     * Carrega um beneficiário existente pelo ID.
     */
    fun loadBeneficiary(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                beneficiary = repository.getProfile(id)
            } catch (e: Exception) {
                SnackbarManager.show(
                    message = e.message ?: "Erro ao carregar beneficiário.",
                    type = SnackbarType.ERROR
                )
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Cria um novo beneficiário.
     */
    fun createBeneficiary(dto: BeneficiaryPostDTO, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                val response: Resposta = repository.postProfile(dto)
                SnackbarManager.show(
                    message = response.message ?: "Beneficiário criado com sucesso.",
                    type = SnackbarType.SUCCESS
                )
                onSuccess?.invoke()
            } catch (e: Exception) {
                SnackbarManager.show(
                    message = e.message ?: "Erro ao criar beneficiário.",
                    type = SnackbarType.ERROR
                )
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Atualiza um beneficiário existente.
     */
    fun updateBeneficiary(id: Int, dto: BeneficiaryPostDTO, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                val response: Resposta = repository.updateProfile(id, dto)
                SnackbarManager.show(
                    message = response.message ?: "Beneficiário atualizado com sucesso.",
                    type = SnackbarType.SUCCESS
                )
                onSuccess?.invoke()
            } catch (e: Exception) {
                SnackbarManager.show(
                    message = e.message ?: "Erro ao atualizar beneficiário.",
                    type = SnackbarType.ERROR
                )
            } finally {
                isLoading = false
            }
        }
    }
}
