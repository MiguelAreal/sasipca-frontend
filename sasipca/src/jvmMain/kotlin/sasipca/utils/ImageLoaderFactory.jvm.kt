package sasipca.utils

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

actual fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    val unsafeClient = HttpClient(CIO) {
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                }
            }
        }
    }

    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(unsafeClient))
        }
        .build()
}