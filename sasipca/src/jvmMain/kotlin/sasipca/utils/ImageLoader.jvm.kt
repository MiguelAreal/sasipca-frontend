package sasipca.utils

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.client.request.header

actual fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    val client = HttpClient(Java) {
        engine {
            protocolVersion = java.net.http.HttpClient.Version.HTTP_2
        }
    }

    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(client))
        }
        .build()
}