package sasipca.screens.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import sasipca.models.Delivery
import sasipca.network.ApiClient
import sasipca.screens.MainScreenContent
import sasipca.storage.SessionManager
import java.time.LocalDate

// ==================================================================
// --- ECRÃS DE LOGIN E MAIN ---
// ==================================================================

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { ApiClient.authRepository }

        sasipca.screens.LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = { navigator.replaceAll(MainScreen()) }
        )
    }
}

class MainScreen : Screen {
    @Composable
    override fun Content() {
        MainScreenContent()
    }
}

// ==================================================================
// --- TABS GERAIS (ADMIN/STAFF) ---
// ==================================================================

object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Início"
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        sasipca.screens.HomeScreen(statsRepository = remember { ApiClient.statsRepository })
    }
}

object ProductsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Produtos"
            val icon = rememberVectorPainter(Icons.Default.Inventory)
            return remember { TabOptions(index = 1u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        sasipca.screens.ProductsScreen(
            productRepository = remember { ApiClient.productRepository },
            onOpenProduct = { barcode -> navigator.push(ProductDetailScreen(barcode)) },
            isReadOnly = false // Admin pode editar
        )
    }
}

object CalendarTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Calendário"
            val icon = rememberVectorPainter(Icons.Default.CalendarToday)
            return remember { TabOptions(index = 2u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        sasipca.screens.CalendarScreen(
            deliveryRepository = remember { ApiClient.deliveryRepository },
            onNavigateToDelivery = { date, isScheduled, delivery ->
                navigator.push(DeliveryScreen(date, isScheduled, delivery))
            }
        )
    }
}

object BeneficiariesTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Beneficiários"
            val icon = rememberVectorPainter(Icons.Default.People)
            return remember { TabOptions(index = 3u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        sasipca.screens.BeneficiariesScreen(
            beneficiaryRepository = remember { ApiClient.beneficiaryRepository },
            onOpenBeneficiary = { id -> navigator.push(BeneficiaryDetailScreen(id)) }
        )
    }
}

// ==================================================================
// --- TABS PARA BENEFICIÁRIO (NOVAS) ---
// ==================================================================

object ViewOnlyProductsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Produtos"
            val icon = rememberVectorPainter(Icons.Default.Inventory)
            return remember { TabOptions(index = 0u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        // Reutiliza ProductsScreen mas em modo leitura
        sasipca.screens.ProductsScreen(
            productRepository = remember { ApiClient.productRepository },
            onOpenProduct = { barcode -> navigator.push(ProductDetailScreen(barcode)) },
            isReadOnly = true
        )
    }
}

object MyProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Meu Perfil"
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember { TabOptions(index = 1u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        // Obtém ID do user logado
        val myUserId = remember { SessionManager.getUserId() } ?: -1

        // Reutiliza BeneficiaryScreen mas em modo leitura/perfil
        sasipca.screens.BeneficiaryScreen(
            beneficiaryId = myUserId,
            repository = remember { ApiClient.beneficiaryRepository },
            deliveryRepository = remember { ApiClient.deliveryRepository },
            isReadOnly = true // Adapta a UI para "Meu Perfil"
        )
    }
}

// ==================================================================
// --- ECRÃS DE DETALHE E OUTROS ---
// ==================================================================

data class BeneficiaryDetailScreen(val beneficiaryId: Int) : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.BeneficiaryScreen(
            beneficiaryId = beneficiaryId,
            repository = remember { ApiClient.beneficiaryRepository },
            deliveryRepository = remember { ApiClient.deliveryRepository },
            isReadOnly = false // Admin a ver detalhe
        )
    }
}

data class ProductDetailScreen(val barcode: String) : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ProductScreen(
            barcode = barcode,
            productRepository = remember { ApiClient.productRepository }
        )
    }
}

data class DeliveryScreen(
    val initialDate: LocalDate? = null,
    val isScheduled: Boolean = false,
    val existingDelivery: Delivery? = null
) : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.DeliveryScreen(
            productRepository = remember { ApiClient.productRepository },
            deliveryRepository = remember { ApiClient.deliveryRepository },
            beneficiaryRepository = remember { ApiClient.beneficiaryRepository },
            initialScheduledDate = initialDate,
            initialIsScheduled = isScheduled,
            existingDelivery = existingDelivery
        )
    }
}

class ReceptionScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ReceiptScreen(
            productRepository = remember { ApiClient.productRepository },
            receiptRepository = remember { ApiClient.receiptRepository }
        )
    }
}

class StockAdjustmentScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.StockAdjustmentScreen(
            adjustmentRepository = remember { ApiClient.adjustmentRepository },
            productRepository = remember { ApiClient.productRepository }
        )
    }
}

class HistoryScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.HistoryScreen(
            historyRepository = remember { ApiClient.historyRepository }
        )
    }
}

class CampaignsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.CampaignsScreen(
            campaignRepository = remember { ApiClient.campaignRepository },
            listsRepository = remember { ApiClient.listsRepository }
        )
    }
}

class ReportsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ReportsScreen(
            reportsRepository = remember { ApiClient.reportRepository },
            beneficiaryRepository = remember { ApiClient.beneficiaryRepository }
        )
    }
}

class NotificationsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.NotificationsScreen(
            notificationRepository = remember { ApiClient.notificationRepository }
        )
    }
}

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.SettingsScreen()
    }
}

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ProfileScreen()
    }
}

class AdminsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.AdminsScreen(
            adminRepository = remember { ApiClient.adminRepository }
        )
    }
}

class StatsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.StatsScreen(
            statsRepository = remember { ApiClient.statsRepository }
        )
    }
}