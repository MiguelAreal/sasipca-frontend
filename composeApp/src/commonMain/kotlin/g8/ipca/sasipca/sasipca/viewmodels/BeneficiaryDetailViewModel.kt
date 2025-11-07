package g8.ipca.sasipca.sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import g8.ipca.sasipca.sasipca.models.BeneficiaryGetDTO
import g8.ipca.sasipca.sasipca.models.BeneficiaryPostDTO
import g8.ipca.sasipca.sasipca.models.Resposta
import g8.ipca.sasipca.sasipca.repositories.BeneficiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel responsável por gerir o estado de UM beneficiário específico
 * Inclui operações de criação, atualização e carregamento
 */
class BeneficiaryDetailViewModel(
    private val repository: BeneficiaryRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var beneficiary by mutableStateOf<BeneficiaryGetDTO?>(null)
        private set

    /**
     * Carrega um beneficiário existente pelo ID
     */
    fun loadBeneficiary(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                errorMessage = null
                beneficiary = repository.getProfile(id)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erro ao carregar beneficiário."
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Cria um novo beneficiário
     */
    fun createBeneficiary(dto: BeneficiaryPostDTO, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                errorMessage = null
                successMessage = null

                val response: Resposta = repository.postProfile(dto)
                successMessage = response.message ?: "Beneficiário criado com sucesso."
                onSuccess?.invoke()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erro ao criar beneficiário."
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Atualiza um beneficiário existente
     */
    fun updateBeneficiary(id: Int, dto: BeneficiaryPostDTO, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                errorMessage = null
                successMessage = null

                val response: Resposta = repository.updateProfile(id, dto)
                successMessage = response.message ?: "Beneficiário atualizado com sucesso."
                onSuccess?.invoke()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erro ao atualizar beneficiário."
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearSuccess() {
        successMessage = null
    }
}
