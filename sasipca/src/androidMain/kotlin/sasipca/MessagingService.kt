package sasipca.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.network.ApiClient
import sasipca.MainActivity
import sasipca.storage.SessionManager

class MessagingService : FirebaseMessagingService() {

    // 1. GESTÃO DE TOKENS (Instalação/Renovação)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM: Novo Token Recebido: $token")

        // Se o utilizador já estiver logado, envia imediatamente para a API
        if (SessionManager.isLoggedInNow()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiClient.notificationRepository.registerDevice(token)
                } catch (e: Exception) {
                    println("FCM: Erro ao enviar novo token: ${e.message}")
                }
            }
        }
    }

    // 2. RECEÇÃO DE MENSAGENS (Foreground/Background)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("FCM: Mensagem recebida de: ${remoteMessage.from}")

        // Verifica se a mensagem traz conteúdo de notificação
        remoteMessage.notification?.let {
            println("FCM: Título: ${it.title}, Corpo: ${it.body}")
            showNotification(it.title ?: "SASIPCA", it.body ?: "")
            sasipca.storage.NotificationManager.refreshCount()        }
    }

    // 3. EXIBIÇÃO DA NOTIFICAÇÃO
    private fun showNotification(title: String, message: String) {
        val channelId = "sasipca_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Criar Canal de Notificação (Obrigatório para Android 8.0+ / API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações Gerais", // Nome visível nas definições do telemóvel
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Alertas de entregas e stock"
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir a App quando clicar na notificação
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir a notificação visual
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Idealmente usa: R.mipmap.ic_launcher
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Remove a notificação ao clicar
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Exibir (ID único baseado no tempo para não substituir as anteriores)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}