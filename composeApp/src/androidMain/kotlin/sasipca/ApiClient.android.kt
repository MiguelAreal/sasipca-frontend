// androidMain/sasipca/createHttpClient.kt

package sasipca

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import sasipca.models.AuthResponse // <-- Importar
import sasipca.repositories.AuthRepository // <-- Remover (não é mais necessário aqui)
import sasipca.storage.SessionManager // <-- Importar
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import io.ktor.client.plugins.auth.* // <-- Importar
import io.ktor.client.plugins.auth.providers.* // <-- Importar
import io.ktor.http.encodedPath

actual fun createHttpClient(
    refreshLogic: suspend () -> Result<AuthResponse> // <-- Assinatura atualizada
): HttpClient {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
    })

    val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, java.security.SecureRandom())
    }
    val trustManager = trustAllCerts[0] as X509TrustManager

    return HttpClient(OkHttp) {
        engine {
            config {
                sslSocketFactory(sslContext.socketFactory, trustManager)
                hostnameVerifier { _, _ -> true }
            }
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = SessionManager.getAccessToken()
                    val refreshToken = SessionManager.getRefreshToken()
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(accessToken, refreshToken)
                    } else null
                }

                refreshTokens {
                    // No access to the request or attributes here anymore
                    val result = refreshLogic()
                    if (result.isSuccess) {
                        val authResponse = result.getOrThrow()
                        BearerTokens(authResponse.accessToken, authResponse.refreshToken)
                    } else {
                        SessionManager.clear()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    val path = request.url.encodedPath
                    path.endsWith("/login") || path.endsWith("/refresh")
                }
            }
        }
    }
}