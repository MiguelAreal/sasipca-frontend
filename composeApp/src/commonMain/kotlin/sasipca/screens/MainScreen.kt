package sasipca.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.launch
import sasipca.screens.navigation.BeneficiariesTab
import sasipca.screens.navigation.CalendarTab
import sasipca.screens.navigation.HomeTab
import sasipca.screens.navigation.ProductsTab

@Composable
fun MainScreen() {
    // 1. Definir a lista de Tabs pela ordem que queres
    val tabs = remember {
        listOf(HomeTab, ProductsTab, CalendarTab, BeneficiariesTab)
    }

    // 2. Configurar o estado do Pager
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Mantemos o TabNavigator para ter acesso ao LocalTabNavigator dentro dos ecrãs
    TabNavigator(HomeTab) {
        val tabNavigator = LocalTabNavigator.current

        // 3. Sincronização: Quando o Pager muda (Swipe), atualiza o TabNavigator
        LaunchedEffect(pagerState.currentPage) {
            tabNavigator.current = tabs[pagerState.currentPage]
        }

        // 4. Sincronização: Quando o TabNavigator muda (ex: via código), atualiza o Pager
        LaunchedEffect(tabNavigator.current) {
            val index = tabs.indexOf(tabNavigator.current)
            if (index != -1 && pagerState.currentPage != index) {
                pagerState.animateScrollToPage(index)
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar {
                    // Renderizamos os itens com base na lista 'tabs'
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = tabNavigator.current == tab,
                            onClick = {
                                // Ao clicar, movemos o Pager (o LaunchedEffect acima tratará de atualizar o Navigator)
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Icon(
                                    painter = tab.options.icon!!,
                                    contentDescription = tab.options.title
                                )
                            },
                            label = { Text(tab.options.title) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            // 5. Em vez de CurrentTab(), usamos o HorizontalPager
            Box(modifier = Modifier.padding(paddingValues)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // Renderiza o conteúdo da Tab correspondente à página
                    tabs[page].Content()
                }
            }
        }
    }
}