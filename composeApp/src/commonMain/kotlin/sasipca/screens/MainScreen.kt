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
import sasipca.navigation.Screen
import sasipca.repositories.ProductRepository
import sasipca.repositories.StockRepository
import sasipca.screens.CalendarScreen
import sasipca.screens.HomeScreen
import sasipca.screens.ProductsScreen
import sasipca.screens.ProfileScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(stockRepository: StockRepository, productRepository: ProductRepository) {
    val tabs = listOf(Screen.Home, Screen.Products, Screen.Calendar, Screen.Profile)
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
                                Screen.Products -> Icons.Default.Inventory
                                Screen.Calendar -> Icons.Default.CalendarMonth
                                Screen.Profile -> Icons.Default.Person
                                else -> Icons.Default.Home
                            }
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            val label = when (screen) {
                                Screen.Home -> "Home"
                                Screen.Products -> "Inventário"
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
                Screen.Products -> ProductsScreen(productRepository)
                Screen.Calendar -> CalendarScreen(stockRepository)
                Screen.Profile -> ProfileScreen()
                else -> Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
