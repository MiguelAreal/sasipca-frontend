package sasipca.utils

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.client.request.header

actual fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    val client = HttpClient(OkHttp) {
        install(DefaultRequest) {
            header(HttpHeaders.UserAgent, "SasIpcaApp/1.0")
        }
    }

    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(client))
        }
        .build()
}