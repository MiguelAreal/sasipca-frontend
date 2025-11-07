package sasipca.storage

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*

suspend inline fun <reified T> HttpClient.authorizedRequest(
    url: String,
    method: HttpMethod = HttpMethod.Get,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    val accessToken = SessionManager.getAccessToken()
        ?: throw Exception("Sem access token")
    val refreshToken = SessionManager.getRefreshToken()
        ?: throw Exception("Sem refresh token")

    return this.request(url) {
        this.method = method
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(HttpHeaders.Cookie, "refreshToken=$refreshToken")
        contentType(ContentType.Application.Json)
        block()
    }.body()
}
