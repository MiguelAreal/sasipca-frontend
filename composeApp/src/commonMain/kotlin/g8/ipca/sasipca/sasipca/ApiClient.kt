package g8.ipca.sasipca.sasipca.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

expect fun createHttpClient(): HttpClient

object ApiClient {
    val client: HttpClient by lazy { createHttpClient() }
}
