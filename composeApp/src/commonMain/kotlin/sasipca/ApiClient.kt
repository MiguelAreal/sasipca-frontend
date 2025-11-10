// commonMain/sasipca/ApiClient.kt

package sasipca

import io.ktor.client.*
import sasipca.models.AuthResponse // Importe o AuthResponse
import sasipca.repositories.AuthRepository
import sasipca.repositories.BeneficiaryRepository
import sasipca.repositories.ProductRepository
import sasipca.repositories.StockRepository

expect fun createHttpClient(
    refreshLogic: suspend () -> Result<AuthResponse>
): HttpClient

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

    fun init() {
        val refreshLogic: suspend () -> Result<AuthResponse> = {
            authRepository.refreshToken()
        }

        // 1. Criar o client
        client = createHttpClient(refreshLogic)

        // 2. Inicializar todos os repositórios com o mesmo client
        authRepository = AuthRepository(client)
        stockRepository = StockRepository(client)
        productRepository = ProductRepository(client)
        beneficiaryRepository = BeneficiaryRepository(client)
    }
}