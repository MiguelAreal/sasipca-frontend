package sasipca.screens

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
import sasipca.screens.navigation.*
import sasipca.storage.SessionManager

// Esta é a função que desenha o ecrã principal com as tabs
@Composable
fun MainScreenContent() {
    // 1. Obter o Role do utilizador
    val userRole = remember { SessionManager.getUserRole() }

    // 2. Definir as tabs com base no Role
    val tabs = remember(userRole) {
        if (userRole == "Beneficiary") {
            // Tabs para Beneficiário
            listOf(ViewOnlyProductsTab, MyProfileTab)
        } else {
            // Tabs para Admin / Staff
            listOf(HomeTab, ProductsTab, CalendarTab, BeneficiariesTab)
        }
    }

    // 3. Estado do Pager (Swipe)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    TabNavigator(tabs.first()) {
        val tabNavigator = LocalTabNavigator.current

        // Sincronização: Swipe -> Tab
        LaunchedEffect(pagerState.currentPage) {
            if (!isProgrammaticScroll) {
                if (pagerState.currentPage < tabs.size) {
                    tabNavigator.current = tabs[pagerState.currentPage]
                }
            }
        }

        // Sincronização: Tab -> Swipe
        LaunchedEffect(tabNavigator.current) {
            val targetIndex = tabs.indexOf(tabNavigator.current)
            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                isProgrammaticScroll = true
                pagerState.animateScrollToPage(targetIndex)
                isProgrammaticScroll = false
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = tabNavigator.current == tab,
                            onClick = { tabNavigator.current = tab },
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
                    if (page < tabs.size) {
                        tabs[page].Content()
                    }
                }
            }
        }
    }
}