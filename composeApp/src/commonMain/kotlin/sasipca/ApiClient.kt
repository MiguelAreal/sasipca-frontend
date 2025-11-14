package sasipca

import io.ktor.client.*
import sasipca.models.AuthResponse
import sasipca.repositories.AuthRepository
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.ListsRepository
import sasipca.repositories.ProductRepository
import sasipca.repositories.StockRepository

expect fun createHttpClient(): HttpClient

object ApiClient {
    lateinit var client: HttpClient
        private set

    lateinit var authRepository: AuthRepository
        private set
    lateinit var stockRepository: StockRepository
        private set
    lateinit var productRepository: ProductRepository
        private set
    lateinit var beneficiaryRepository: BeneficiaryRepository
        private set

    lateinit var listsRepository: ListsRepository
        private set

    fun init() {
        // Criar HttpClient sem lógica de refresh automática
        client = createHttpClient()

        // Criar o AuthRepository com o client
        authRepository = AuthRepository(client)

        // Criar os restantes repositórios
        stockRepository = StockRepository(client)
        productRepository = ProductRepository(client)
        beneficiaryRepository = BeneficiaryRepository(client)
        listsRepository = ListsRepository(client)
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        // Delegar para o AuthRepository
        return authRepository.refreshToken()
    }
}
