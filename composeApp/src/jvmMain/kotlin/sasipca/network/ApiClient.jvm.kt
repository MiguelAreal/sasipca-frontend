package sasipca.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import io.ktor.client.plugins.logging.*

actual fun createHttpClient(): HttpClient {
    return HttpClient(CIO) {
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                }
            }
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.SIMPLE
        }
    }
}
