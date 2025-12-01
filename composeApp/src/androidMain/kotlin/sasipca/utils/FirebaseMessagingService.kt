package sasipca.android

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import sasipca.storage.SessionManager
// Importar APIs de notificação nativa do Android (NotificationChannel, etc.)

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // IMPORTANTE: Enviar este token para o teu Backend .NET
        // para salvar na tabela de Users.
        // ex: ApiClient.updateFcmToken(token)
        SessionManager.saveFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Quando a notificação chega, mostra no Android
        remoteMessage.notification?.let {
            showSystemNotification(it.title, it.body)
        }
    }

    private fun showSystemNotification(title: String?, body: String?) {
        // Código padrão do Android para criar NotificationChannel e NotificationManager
        // ...
    }
}