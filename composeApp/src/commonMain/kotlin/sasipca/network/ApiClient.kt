package sasipca.network

import io.ktor.client.*
import sasipca.auth.MicrosoftAuthManager // <--- Importante
import sasipca.models.AuthResponse
import sasipca.repositories.* // (Simplificado imports)

expect fun createHttpClient(): HttpClient

object ApiClient {
    lateinit var client: HttpClient
        private set

    lateinit var authRepository: AuthRepository
        private set
    lateinit var deliveryRepository: DeliveryRepository
        private set
    lateinit var receiptRepository: ReceiptRepository
        private set
    lateinit var productRepository: ProductRepository
        private set
    lateinit var beneficiaryRepository: BeneficiaryRepository
        private set
    lateinit var campaignRepository: CampaignRepository
        private set
    lateinit var reportRepository: ReportsRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var listsRepository: ListsRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set
    lateinit var adjustmentRepository : AdjustmentRepository
        private set

    lateinit var adminRepository: AdminRepository
        private set

    fun init(authManager: MicrosoftAuthManager) {

        // Criar HttpClient
        client = createHttpClient()
        authRepository = AuthRepository(client, authManager)
        deliveryRepository = DeliveryRepository(client)
        receiptRepository = ReceiptRepository(client)
        productRepository = ProductRepository(client)
        beneficiaryRepository = BeneficiaryRepository(client)
        listsRepository = ListsRepository(client)
        campaignRepository = CampaignRepository(client)
        reportRepository = ReportsRepository(client)
        historyRepository = HistoryRepository(client)
        notificationRepository = NotificationRepository(client)
        adjustmentRepository = AdjustmentRepository(client)
        adminRepository = AdminRepository(client)
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        return authRepository.refreshToken()
    }
}