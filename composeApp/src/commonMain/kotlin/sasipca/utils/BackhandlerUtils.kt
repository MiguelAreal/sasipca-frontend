package sasipca.utils

import androidx.compose.runtime.Composable
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen

/**
 * Método que diverge entre plataformas para ação de navegar para trás.
 */
@Composable
expect fun SafeBackHandler(enabled: Boolean = true, onBack: () -> Unit)


/**
 * Navegação 'para trás'.
 *
 * Verifica se pode ir para trás e se o ecrã é um overlay, e não um separador
 */
@Composable
fun HandleBackNavigation(currentScreen: Screen) {
    SafeBackHandler(
        enabled = NavigationService.canGoBack() && Screen.isOverlay(currentScreen)
    ) {
        NavigationService.goBack()
    }
}