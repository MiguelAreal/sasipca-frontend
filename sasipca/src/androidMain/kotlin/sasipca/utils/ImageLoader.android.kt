package sasipca.utils

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun getAsyncImageLoader(context: PlatformContext): ImageLoader {

    val client = HttpClient(OkHttp)

    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(client))
        }
        .crossfade(true)
        .build()
}