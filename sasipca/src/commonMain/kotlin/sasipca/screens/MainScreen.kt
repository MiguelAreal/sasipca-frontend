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
import sasipca.navigation.BeneficiariesTab
import sasipca.navigation.CalendarTab
import sasipca.navigation.HomeTab
import sasipca.navigation.MyProfileTab
import sasipca.navigation.ProductsTab
import sasipca.navigation.ViewOnlyProductsTab
import sasipca.storage.SessionManager

@Composable
fun MainScreenContent(openCalendar: Boolean = false) {
    val userRole = remember { SessionManager.getUserRole() }

    val tabs = remember(userRole) {
        if (userRole == "Beneficiary") {
            listOf(ViewOnlyProductsTab, MyProfileTab)
        } else {
            listOf(HomeTab, ProductsTab, CalendarTab, BeneficiariesTab)
        }
    }

    // Lógica para decidir a tab inicial
    val initialTab = remember(openCalendar, tabs) {
        if (openCalendar && tabs.contains(CalendarTab)) {
            CalendarTab
        } else {
            tabs.first()
        }
    }

    // Inicializa o TabNavigator com a tab correta
    TabNavigator(initialTab) {
        val tabNavigator = LocalTabNavigator.current

        // 3. Estado do Pager (Swipe)
        // O initialPage deve corresponder à Tab inicial para evitar "saltos" visuais
        val initialPageIndex = remember(initialTab) { tabs.indexOf(initialTab) }
        val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { tabs.size })

        var isProgrammaticScroll by remember { mutableStateOf(false) }

        // Se o openCalendar mudar depois da criação (ex: onNewIntent no Android), força a navegação
        LaunchedEffect(openCalendar) {
            if (openCalendar && tabs.contains(CalendarTab) && tabNavigator.current != CalendarTab) {
                tabNavigator.current = CalendarTab
            }
        }

        // Sincronização: Swipe → Tab
        LaunchedEffect(pagerState.currentPage) {
            if (!isProgrammaticScroll) {
                if (pagerState.currentPage < tabs.size) {
                    val targetTab = tabs[pagerState.currentPage]
                    if (tabNavigator.current != targetTab) {
                        tabNavigator.current = targetTab
                    }
                }
            }
        }

        // Sincronização: Tab → Swipe
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