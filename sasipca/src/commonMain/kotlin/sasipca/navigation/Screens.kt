    package sasipca.screens.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import sasipca.network.ApiClient
import sasipca.models.Delivery
import java.time.LocalDate

// ==================================================================
// --- ECRÃS DE AUTENTICAÇÃO E CONTAINER ---
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
        // Chama a função visual que está no outro ficheiro
        MainScreenContent()
    }
}


// ==================================================================
// --- BOTTOM TABS (Principal) ---
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
        sasipca.screens.HomeScreen(
            statsRepository = remember { ApiClient.statsRepository }
        )
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
        // Usa o navigator principal para sair da tab ao clicar num item
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        sasipca.screens.ProductsScreen(
            productRepository = remember { ApiClient.productRepository },
            onOpenProduct = { barcode -> navigator.push(ProductDetailScreen(barcode)) }
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
// --- ECRÃS DE DETALHE E FUNCIONAIS ---
// ==================================================================

// Detalhe de Beneficiário
data class BeneficiaryDetailScreen(val beneficiaryId: Int) : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.BeneficiaryScreen(
            beneficiaryId = beneficiaryId,
            repository = remember { ApiClient.beneficiaryRepository },
            deliveryRepository = remember { ApiClient.deliveryRepository }
        )
    }
}

// Detalhe de Produto
data class ProductDetailScreen(val barcode: String) : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ProductScreen(
            barcode = barcode,
            productRepository = remember { ApiClient.productRepository },
            // historyRepository usa o default
        )
    }
}

// Criar ou Editar Entrega
data class DeliveryScreen(
    val initialDate: LocalDate? = null,
    val isScheduled: Boolean = false,
    val existingDelivery: sasipca.models.Delivery? = null // NOVO PARÂMETRO
) : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.DeliveryScreen(
            productRepository = remember { ApiClient.productRepository },
            deliveryRepository = remember { ApiClient.deliveryRepository },
            beneficiaryRepository = remember { ApiClient.beneficiaryRepository },
            initialScheduledDate = initialDate,
            initialIsScheduled = isScheduled,
            existingDelivery = existingDelivery // Passa para o ecrã
        )
    }
}
// Receção de Stock
class ReceptionScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ReceiptScreen(
            productRepository = remember { ApiClient.productRepository },
            receiptRepository = remember { ApiClient.receiptRepository }
        )
    }
}

// Ajuste de Stock
class StockAdjustmentScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.StockAdjustmentScreen(
            adjustmentRepository = remember { ApiClient.adjustmentRepository },
            productRepository = remember { ApiClient.productRepository }
        )
    }
}

// Histórico Global
class HistoryScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.HistoryScreen(
            historyRepository = remember { ApiClient.historyRepository }
        )
    }
}

// Campanhas
class CampaignsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.CampaignsScreen(
            campaignRepository = remember { ApiClient.campaignRepository },
            listsRepository = remember { ApiClient.listsRepository }
        )
    }
}

// Relatórios
class ReportsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ReportsScreen(
            // 1. ReportRepo para listar e gerar
            reportsRepository = remember { ApiClient.reportRepository },
            // 2. BeneficiaryRepo para o autocomplete no Dialog
            beneficiaryRepository = remember { ApiClient.beneficiaryRepository }
        )
    }
}

// Notificações
class NotificationsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.NotificationsScreen(
            notificationRepository = remember { ApiClient.notificationRepository }
        )
    }
}

// Definições
class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.SettingsScreen()
    }
}

// Perfil
class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        sasipca.screens.ProfileScreen()
    }
}

// Gestão de Administradores
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