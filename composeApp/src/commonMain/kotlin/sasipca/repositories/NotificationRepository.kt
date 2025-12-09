package sasipca.repositories

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import sasipca.storage.ApiConfig
import sasipca.storage.requestWithAuth

@Serializable
data class DeviceTokenDto(val token: String)

class NotificationRepository(private val client: HttpClient) {

    // Envia o token FCM para o backend guardar na tabela UserDevices
    suspend fun registerDevice(token: String) {
        try {
            val url = "${ApiConfig.baseUrl()}/notifications/device"
            println("NotificationRepo: A enviar token para $url")

            client.requestWithAuth<Unit>(
                method = HttpMethod.Post,
                url = url,
                body = DeviceTokenDto(token)
            )
            println("NotificationRepo: Token FCM registado com sucesso no backend.")
        } catch (e: Exception) {
            println("NotificationRepo: ERRO ao registar dispositivo: ${e.message}")
            e.printStackTrace()
        }
    }
}