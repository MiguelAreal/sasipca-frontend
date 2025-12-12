package sasipca.utils

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    // 1. Criar um TrustManager que não valida nada
    val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    // 2. Configurar o SSL Context
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    // 3. Criar o Cliente Ktor usando o motor OkHttp
    val unsafeClient = HttpClient(OkHttp) {
        engine {
            config {
                sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                hostnameVerifier { _, _ -> true } // Aceita qualquer hostname (localhost, 10.0.2.2, etc)
            }
        }
    }

    // 4. Retornar o ImageLoader do Coil
    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(unsafeClient))
        }
        .build()
}