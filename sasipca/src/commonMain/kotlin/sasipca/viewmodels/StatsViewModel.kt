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

    // --- CONSTANTES DE TIPO DE MOVIMENTO (Igual ao Backend) ---
    private val MOVEMENT_TYPE_IN = 1
    private val MOVEMENT_TYPE_OUT = 2

    // --- ESTADOS DE UI (GLOBAL) ---
    var isLoading by mutableStateOf(false)
        private set

    // --- ESTADOS PARA A HOME PAGE ---
    var isHomeLoading by mutableStateOf(false)
        private set

    var monthlyStats by mutableStateOf<MonthlySummary?>(null)
        private set

    var currentHomeMonth by mutableStateOf(YearMonth.now())
        private set

    // --- ESTADOS PARA ESTATÍSTICAS AVANÇADAS ---
    var summary by mutableStateOf<DashboardSummary?>(null)
        private set

    var movementsFlow by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set

    var topProducts by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set

    // A UI continua a ter variáveis separadas, mas são preenchidas pelo mesmo endpoint
    var categoriesDataOut by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set
    var categoriesDataIn by mutableStateOf<List<ChartDataPoint>>(emptyList())
        private set

    // --- FILTROS DE DATA ---
    var selectedRange by mutableStateOf(TimeRange.MONTH)
        private set

    var showDatePicker by mutableStateOf(false)
        private set

    var startDate by mutableStateOf(LocalDate.now().minusDays(30))
        private set
    var endDate by mutableStateOf(LocalDate.now())
        private set

    // =========================================================
    // 1. CARREGAR DADOS DA HOME (MENSAL)
    // =========================================================

    fun loadHomeStats() {
        isHomeLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                monthlyStats = repository.getMonthlySummary(
                    month = currentHomeMonth.monthValue,
                    year = currentHomeMonth.year
                )
            } catch (e: Exception) {
                println("Erro home stats: ${e.message}")
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

    // =========================================================
    // 2. CARREGAR ESTATÍSTICAS AVANÇADAS (SCREEN DETALHADO)
    // =========================================================

    fun loadAllAdvancedStats() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startStr = startDate.toString()
                val endStr = endDate.toString()

                // 1. KPI Global
                summary = repository.getSummary(startStr, endStr)

                // 2. Gráficos de Linha e Top
                movementsFlow = repository.getMovementsFlow(startStr, endStr)
                topProducts = repository.getTopProducts(startStr, endStr)

                // 3. Distribuição de Categorias (Chamadas ao endpoint consolidado)
                categoriesDataIn = repository.getCategoriesDistribution(MOVEMENT_TYPE_IN, startStr, endStr)
                categoriesDataOut = repository.getCategoriesDistribution(MOVEMENT_TYPE_OUT, startStr, endStr)

            } catch (e: Exception) {
                println("Erro advanced stats: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // =========================================================
    // 3. LÓGICA DE FILTROS
    // =========================================================

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
        showDatePicker = false
        if (start != null && end != null) {
            updateDateRange(start, end)
        } else {
            if (selectedRange == TimeRange.CUSTOM) {
                selectedRange = TimeRange.MONTH
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
}