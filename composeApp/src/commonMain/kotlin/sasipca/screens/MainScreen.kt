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
import sasipca.screens.CalendarScreen
import sasipca.screens.HomeScreen
import sasipca.screens.ProductsScreen
import kotlinx.coroutines.launch
import sasipca.navigation.NavigationService
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.CampaignRepository
import sasipca.repositories.DeliveryRepository
import sasipca.screens.BeneficiariesScreen

@Composable
fun MainScreen(deliveryRepository: DeliveryRepository,
               productRepository: ProductRepository,
               beneficiaryRepository: BeneficiaryRepository,
               campaignRepository: CampaignRepository,
               onOpenBeneficiary: (Int) -> Unit = {},onOpenProduct: (String) -> Unit = {})
{
    val tabs = listOf(Screen.Home, Screen.Products, Screen.Calendar, Screen.Beneficiaries)
    val pagerState = rememberPagerState(
        initialPage = NavigationService.mainScreenTabIndex,
        pageCount ={ tabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        NavigationService.mainScreenTabIndex = pagerState.currentPage
    }

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
                                Screen.Beneficiaries -> Icons.Default.People
                                else -> Icons.Default.Home
                            }
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            val label = when (screen) {
                                Screen.Home -> "Home"
                                Screen.Products -> "Inventário"
                                Screen.Calendar -> "Calendário"
                                Screen.Beneficiaries -> "Beneficiários"
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
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) { page ->
            val screen = tabs[page]

            // Só compõe a página atual
            if (page == pagerState.currentPage) {
                when (screen) {
                    Screen.Home -> HomeScreen()
                    Screen.Products -> ProductsScreen(productRepository,onOpenProduct)
                    Screen.Calendar -> CalendarScreen(deliveryRepository)
                    Screen.Beneficiaries -> BeneficiariesScreen(
                        beneficiaryRepository = beneficiaryRepository,
                        onOpenBeneficiary = onOpenBeneficiary
                    )
                    else -> Box(modifier = Modifier.fillMaxSize())
                }
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

    }
}
