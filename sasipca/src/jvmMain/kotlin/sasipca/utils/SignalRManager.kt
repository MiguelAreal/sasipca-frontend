package sasipca.utils

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.*
import sasipca.network.ApiClient
import sasipca.network.ApiConfig
import sasipca.storage.NotificationManager
import sasipca.storage.SessionManager
import kotlin.coroutines.coroutineContext

class SignalRManager(
    private val onNotificationReceived: (String, String) -> Unit
) {
    private var hubConnection: HubConnection? = null

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun start() {
        // Se já estiver conectado, não faz nada
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        // Usamos Single.defer para que a função lambda seja executada a cada tentativa de conexão.
        // Assim, obtemos sempre o token mais recente do SessionManager.
        val tokenProvider = Single.defer {
            val currentToken = SessionManager.getAccessToken()
            if (currentToken != null) {
                Single.just(currentToken)
            } else {
                Single.error(Exception("Sem token de acesso"))
            }
        }

        // Configuração
        hubConnection = HubConnectionBuilder
            .create("${ApiConfig.baseUrl()}/notification-hub")
            .withAccessTokenProvider(tokenProvider) // Passamos o provider dinâmico
            .build()

        hubConnection?.onClosed { error ->
            println("SignalR: ligação perdida (${error?.message}). A tentar reconectar...")
            GlobalScope.launch(Dispatchers.IO) {
                while (isActive) {
                    try {
                        hubConnection?.start()?.blockingAwait()
                        println("SignalR: reconectado!")
                        break
                    } catch (e: Exception) {
                        delay(3000)
                    }
                }
            }
        }

        hubConnection?.on("ReceiveNotification", { title: String, msg: String ->
            NotificationManager.refreshCount()
            onNotificationReceived(title, msg)
        }, String::class.java, String::class.java)

        // Loop de Tentativa de Conexão Inicial
        while (coroutineContext.isActive) {
            // Verifica se o token existe antes de tentar (evita spam de erro se fez logout)
            if (SessionManager.getAccessToken() == null) return

            try {
                println("SignalR: A tentar conectar...")
                hubConnection?.start()?.blockingAwait()
                println("SignalR: Conectado com sucesso!")
                return
            } catch (e: Exception) {
                println("SignalR: Falha ao conectar (${e.message}). Nova tentativa em 5s...")
                try {
                    delay(5000)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    fun stop() {
        try {
            hubConnection?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}