import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import g8.ipca.sasipca.sasipca.navigation.Screen
import g8.ipca.sasipca.sasipca.screens.CalendarScreen
import g8.ipca.sasipca.sasipca.screens.HomeScreen
import g8.ipca.sasipca.sasipca.screens.ProfileScreen
import g8.ipca.sasipca.sasipca.screens.StockScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(onThemeChanged: (Boolean) -> Unit = {}) {
    val tabs = listOf(Screen.Home, Screen.Stock, Screen.Calendar, Screen.Profile)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount ={ tabs.size }
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = {
                            val icon = when (screen) {
                                Screen.Home -> Icons.Default.Home
                                Screen.Stock -> Icons.Default.Inventory
                                Screen.Calendar -> Icons.Default.CalendarMonth
                                Screen.Profile -> Icons.Default.Person
                                else -> Icons.Default.Home
                            }
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            val label = when (screen) {
                                Screen.Home -> "Home"
                                Screen.Stock -> "Inventário"
                                Screen.Calendar -> "Calendário"
                                Screen.Profile -> "Perfil"
                                else -> ""
                            }
                            Text(label)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (tabs[page]) {
                Screen.Home -> HomeScreen()
                Screen.Stock -> StockScreen()
                Screen.Calendar -> CalendarScreen()
                Screen.Profile -> ProfileScreen()
                else -> Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
