// desktopMain/sasipca/createHttpClient.kt

package sasipca

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import sasipca.models.AuthResponse
import sasipca.storage.SessionManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.http.encodedPath
import sasipca.storage.RefreshTokenRequestKey

actual fun createHttpClient(
    refreshLogic: suspend () -> Result<AuthResponse>
): HttpClient {
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