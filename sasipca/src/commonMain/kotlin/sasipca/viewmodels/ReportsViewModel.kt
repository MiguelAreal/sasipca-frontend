package sasipca.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sasipca.models.*
import sasipca.repositories.ReportsRepository
import sasipca.utils.FileSaver

data class ReportsUiState(
    val isLoading: Boolean = false,
    val reports: List<ReportGetDTO> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class ReportsViewModel(
    private val repository: ReportsRepository,
    private val fileSaver: FileSaver
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    init {
        loadReports()
    }

    fun loadReports() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = repository.getGeneratedReports()
                _uiState.value = _uiState.value.copy(isLoading = false, reports = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun downloadExistingReport(report: ReportGetDTO) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = repository.downloadReport(report.id)

                val extension = if (report.name.endsWith(".csv")) "" else if (report.name.endsWith(".pdf")) "" else ".pdf"
                val fileName = if(report.name.contains(".")) report.name else "${report.name}$extension"

                fileSaver.saveFile(fileName, bytes,true)

                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Ficheiro guardado: $fileName")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao transferir: ${e.message}")
            }
        }
    }

    // --- CORREÇÃO AQUI ---
    // Adicionados os parâmetros 'status' e 'beneficiaryId'
    fun generateNewReport(
        type: ReportTypesEnum,
        format: ReportFormat,
        fileName: String,
        startDate: String?,
        endDate: String?,
        movementId: String?,
        status: Int? = null,
        beneficiaryId: Int? = null
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (type == ReportTypesEnum.MovementDetails && movementId.isNullOrBlank()) {
                    throw Exception("ID do Movimento é obrigatório para este tipo.")
                }

                val ext = if (format == ReportFormat.PDF) ".pdf" else ".csv"
                val finalName = if (fileName.endsWith(ext)) fileName else "$fileName$ext"

                val request = ReportRequestDTO(
                    type = type.value,
                    format = format.value,
                    fileName = fileName,
                    filters = ReportFiltersDTO(
                        startDate = startDate,
                        endDate = endDate,
                        status = status,
                        beneficiaryId = beneficiaryId
                    ),
                    targetMovementId = movementId?.toIntOrNull()
                )

                val bytes = repository.generateReport(request)
                fileSaver.saveFile(finalName, bytes,true)

                val list = repository.getGeneratedReports()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reports = list,
                    successMessage = "Relatório gerado e transferido!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}