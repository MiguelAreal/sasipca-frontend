package sasipca

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
    lateinit var listsRepository: ListsRepository
        private set

    // ALTERAÇÃO AQUI: Recebe o manager como parâmetro
    fun init(authManager: MicrosoftAuthManager) {

        // Criar HttpClient
        client = createHttpClient()

        // Passar o manager para o AuthRepository
        authRepository = AuthRepository(client, authManager)

        // Criar os restantes repositórios (iguais)
        deliveryRepository = DeliveryRepository(client)
        receiptRepository = ReceiptRepository(client)
        productRepository = ProductRepository(client)
        beneficiaryRepository = BeneficiaryRepository(client)
        listsRepository = ListsRepository(client)
        campaignRepository = CampaignRepository(client)
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        return authRepository.refreshToken()
    }
}