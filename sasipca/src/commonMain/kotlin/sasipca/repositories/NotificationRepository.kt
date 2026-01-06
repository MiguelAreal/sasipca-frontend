package sasipca.repositories

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import sasipca.models.Notification
import sasipca.network.ApiConfig
import sasipca.network.requestWithAuth

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

    // Obter lista de notificações
    suspend fun getNotifications(): List<Notification> {
        return try {
            client.requestWithAuth<List<Notification>>(
                method = HttpMethod.Get,
                url = "${ApiConfig.baseUrl()}/notifications"
            )
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Obter contagem de não lidas (Distintivo)
    suspend fun getUnreadCount(): Int {
        return try {
            client.requestWithAuth<Int>(
                method = HttpMethod.Get,
                url = "${ApiConfig.baseUrl()}/notifications/unread-count"
            )
        } catch (_: Exception) {
            0
        }
    }

    // Marcar como lida
    suspend fun markAsRead(id: Int): Boolean {
        return try {
            client.requestWithAuth<Unit>(
                method = HttpMethod.Put,
                url = "${ApiConfig.baseUrl()}/notifications/$id/read"
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    // Arquivar
    suspend fun archive(id: Int): Boolean {
        return try {
            client.requestWithAuth<Unit>(
                method = HttpMethod.Delete,
                url = "${ApiConfig.baseUrl()}/notifications/$id"
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}