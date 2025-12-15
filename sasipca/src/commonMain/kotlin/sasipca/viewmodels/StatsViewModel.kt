package sasipca.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.models.ChartDataPoint
import sasipca.models.DashboardSummary
import sasipca.models.MonthlySummary
import sasipca.repositories.StatsRepository
import java.time.LocalDate
import java.time.YearMonth

enum class TimeRange(val label: String, val days: Int) {
    WEEK("7 Dias", 7),
    MONTH("30 Dias", 30),
    QUARTER("3 Meses", 90),
    YEAR("Este Ano", 365),
    CUSTOM("Personalizado", 0)
}

class StatsViewModel(private val repository: StatsRepository) : ViewModel() {

    // --- ESTADOS GERAIS ---
    var isLoading by mutableStateOf(false)
        private set
    var isHomeLoading by mutableStateOf(false)
        private set

    // --- DADOS ANALÍTICOS ---
    var summary by mutableStateOf<DashboardSummary?>(null)
        private set
    var movementsFlow by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set
    var topProducts by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set
    var categoriesData by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set

    // --- DADOS HOME ---
    var monthlyStats by mutableStateOf<MonthlySummary?>(null)
        private set
    var currentHomeMonth by mutableStateOf(YearMonth.now())
        private set

    // --- FILTROS ---
    var selectedRange by mutableStateOf(TimeRange.MONTH)
        private set

    // Estado para controlar a visibilidade do Dialog de Datas
    var showDatePicker by mutableStateOf(false)
        private set

    // Datas atuais usadas na query
    var startDate by mutableStateOf(LocalDate.now().minusDays(30))
        private set
    var endDate by mutableStateOf(LocalDate.now())
        private set

    // =========================================================
    // LÓGICA DE ESTATÍSTICAS AVANÇADAS
    // =========================================================

    fun loadAllAdvancedStats() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startStr = startDate.toString()
                val endStr = endDate.toString()

                // Carregamento paralelo (na prática, sequencial em coroutine scope)
                summary = repository.getSummary()
                movementsFlow = repository.getMovementsFlow(startStr, endStr)
                topProducts = repository.getTopProducts(startStr, endStr)
                categoriesData = repository.getCategoriesDistribution(startStr, endStr)

            } catch (e: Exception) {
                println("Erro Stats: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        selectedRange = range
        if (range == TimeRange.CUSTOM) {
            showDatePicker = true
        } else {
            val end = LocalDate.now()
            val start = when(range) {
                TimeRange.YEAR -> LocalDate.of(end.year, 1, 1)
                else -> end.minusDays(range.days.toLong())
            }
            updateDateRange(start, end)
        }
    }

    fun onCustomDatesSelected(start: LocalDate?, end: LocalDate?) {
        showDatePicker = false // Fecha o dialog

        if (start != null && end != null) {
            updateDateRange(start, end)
        } else {
            // Se o utilizador cancelou ou não escolheu datas válidas,
            // revertemos o selecionador visual para algo seguro (ex: Mês) se estava em Custom
            if (selectedRange == TimeRange.CUSTOM) {
                // Opcional: Voltar ao anterior ou manter custom mas sem recarregar
                selectedRange = TimeRange.MONTH
                // Recarrega o default para garantir consistência
                val end = LocalDate.now()
                updateDateRange(end.minusDays(30), end)
            }
        }
    }

    private fun updateDateRange(start: LocalDate, end: LocalDate) {
        startDate = start
        endDate = end
        loadAllAdvancedStats()
    }

    // =========================================================
    // LÓGICA DA HOME PAGE
    // =========================================================
    fun loadHomeStats() {
        viewModelScope.launch(Dispatchers.IO){
            isHomeLoading = true
            try {
                monthlyStats = repository.getMonthlySummary(currentHomeMonth.monthValue, currentHomeMonth.year)
            } catch (_: Exception) {
                monthlyStats = null
            } finally {
                isHomeLoading = false
            }
        }
    }

    fun nextMonth() {
        currentHomeMonth = currentHomeMonth.plusMonths(1)
        loadHomeStats()
    }

    fun prevMonth() {
        currentHomeMonth = currentHomeMonth.minusMonths(1)
        loadHomeStats()
    }
}