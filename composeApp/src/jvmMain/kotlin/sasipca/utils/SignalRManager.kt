package sasipca.utils

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.core.Single
import sasipca.storage.ApiConfig
import sasipca.storage.SessionManager
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

class SignalRManager {
    private var hubConnection: HubConnection? = null


    fun trustAllCertificates() {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

        HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifier { _, _ -> true })
    }


    fun start() {

        val token = SessionManager.getAccessToken() ?: return

        try {
            trustAllCertificates()

            hubConnection = HubConnectionBuilder
                .create("${ApiConfig.baseUrl()}/notification-hub")
                .withAccessTokenProvider(Single.just(token))
                .build()

            hubConnection?.on("ReceiveNotification", { title: String, msg: String ->
                showDesktopNotification(title, msg)
            }, String::class.java, String::class.java)

            hubConnection?.start()?.blockingAwait()
            println("SignalR Desktop: Conectado!")

        } catch (e: Exception) {
            println("SignalR Desktop: Erro ao conectar - ${e.message}")
        }
    }

    fun stop() {
        try {
            hubConnection?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showDesktopNotification(title: String, message: String) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val existingIcon = tray.trayIcons.firstOrNull()

            if (existingIcon != null) {
                existingIcon.displayMessage(title, message, MessageType.INFO)
            } else {
                val resource = javaClass.getResource("/icon.png")
                if (resource != null) {
                    val image = Toolkit.getDefaultToolkit().createImage(resource)
                    val trayIcon = TrayIcon(image, "SASIPCA")
                    trayIcon.isImageAutoSize = true
                    tray.add(trayIcon)
                    trayIcon.displayMessage(title, message, MessageType.INFO)
                } else {
                    println("Erro: Ícone não encontrado para notificação.")
                }
            }
        }
    }
}
