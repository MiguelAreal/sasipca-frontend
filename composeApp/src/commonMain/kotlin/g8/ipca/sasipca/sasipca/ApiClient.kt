package g8.ipca.sasipca.sasipca.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient

object ApiClient {
    val client: HttpClient by lazy { createHttpClient() }
}
