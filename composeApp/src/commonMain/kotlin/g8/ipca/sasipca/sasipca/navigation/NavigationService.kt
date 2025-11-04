package g8.ipca.sasipca.sasipca.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Representa todos os ecrãs do app.
 * Inclui tabs, overlays e ecrãs independentes.
 */
sealed class Screen {
    object Login : Screen()

    object Main : Screen()
    object Home : Screen()
    object Stock : Screen()
    object Calendar : Screen()
    object Profile : Screen()
    object Reception : Screen()
    object Delivery : Screen()
    object StockAdjustment : Screen()
    object Campaigns : Screen()
    object Beneficiaries : Screen()
    object Settings : Screen()
    object Notifications : Screen()

    object Placeholder : Screen()

    companion object {

        /** Define quais são ecrãs "principais" (tabs) */
        val bottomTabs = setOf(Home, Stock, Calendar, Profile)

        /** Retorna true se o ecrã for overlay (ou seja, não é uma tab principal) */
        fun isOverlay(screen: Screen): Boolean = screen !in bottomTabs && screen !is Login && screen !is Main
        fun isSettings(screen:Screen):Boolean = screen !is Settings
    }
}


/**
 * Serviço global de navegação com histórico e animações.
 */
object NavigationService {
    private val backStack = mutableListOf<Screen>()

    var currentScreen by mutableStateOf<Screen>(Screen.Login)
        private set

    var previousScreen by mutableStateOf<Screen?>(null)
        private set

    /** Navega para um ecrã e guarda o atual no histórico */
    fun navigateTo(screen: Screen) {
        previousScreen = currentScreen
        backStack.add(currentScreen)
        currentScreen = screen
    }

    /** Regressa ao ecrã anterior */
    fun goBack() {
        if (backStack.isNotEmpty()) {
            previousScreen = currentScreen
            currentScreen = backStack.removeAt(backStack.lastIndex)
        }
    }

    /** Limpa o histórico e vai diretamente para um ecrã */
    fun resetTo(screen: Screen) {
        backStack.clear()
        previousScreen = null
        currentScreen = screen
    }

    /** Verifica se pode voltar */
    fun canGoBack(): Boolean = backStack.isNotEmpty()
}
