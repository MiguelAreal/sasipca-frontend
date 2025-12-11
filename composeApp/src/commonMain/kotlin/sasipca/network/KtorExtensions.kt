package sasipca.network

import io.ktor.client.request.*
import io.ktor.util.*

/**
 * Chave de atributo para marcar um pedido como sendo o de "refresh token".
 * Usado pelo interceptor Auth para evitar loops infinitos.
 */
val RefreshTokenRequestKey = AttributeKey<Boolean>("RefreshTokenRequestKey")

/**
 * Função de extensão que marca um pedido como sendo o de refresh.
 */
fun HttpRequestBuilder.markAsRefreshTokenRequest() {
    attributes.put(RefreshTokenRequestKey, true)
}