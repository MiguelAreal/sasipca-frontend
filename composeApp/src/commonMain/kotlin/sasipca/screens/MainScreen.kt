package sasipca.screens.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator

@Composable
fun MainScreenContent() {
    // 1. Definir as tabs
    val tabs = remember {
        listOf(HomeTab, ProductsTab, CalendarTab, BeneficiariesTab)
    }

    // 2. Estado do Pager
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    TabNavigator(HomeTab) {
        val tabNavigator = LocalTabNavigator.current

        // 3. Sincronização: Pager -> Tab (Swipe do utilizador)
        LaunchedEffect(pagerState.currentPage) {
            if (!isProgrammaticScroll) {
                tabNavigator.current = tabs[pagerState.currentPage]
            }
        }

        // 4. Sincronização: Tab -> Pager (Clique na BottomBar ou Navegação interna)
        LaunchedEffect(tabNavigator.current) {
            val targetIndex = tabs.indexOf(tabNavigator.current)

            // Se o pager não estiver na página certa, animamos até lá
            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                isProgrammaticScroll = true // Bloqueia o listener de cima
                pagerState.animateScrollToPage(targetIndex)
                isProgrammaticScroll = false // Liberta o listener quando acabar
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = tabNavigator.current == tab,
                            onClick = {
                                tabNavigator.current = tab
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
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    tabs[page].Content()
                }
            }
        }
    }
}