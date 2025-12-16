package sasipca

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sasipca.auth.MicrosoftAuthManagerAndroid
import sasipca.network.ApiClient
import sasipca.storage.SessionManager
import sasipca.storage.SettingsManager
import sasipca.ui.theme.SasIpcaTheme
import sasipca.utils.ObserveScreenSize

class MainActivity : ComponentActivity() {

    private lateinit var msAuthManager: MicrosoftAuthManagerAndroid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Inicializar contexto estático
        sasipca.utils.AndroidContext.set(this)

        // 1. Pedir permissões de notificação (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // 2. Inicializar Settings e Sessão
        val settings: Settings = SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(this))
        SessionManager.init(settings)
        SettingsManager.init(settings) // O SettingsManager deve ser inicializado antes de usares no Firebase

        // 3. Inicializar Auth e API
        msAuthManager = MicrosoftAuthManagerAndroid(this)
        lifecycleScope.launch {
            msAuthManager.init()
        }
        ApiClient.init(msAuthManager)

        // 4. Lógica Firebase (Token FCM)
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    println("FCM: ERRO ao obter token: ${task.exception?.message}")
                    task.exception?.printStackTrace()
                    return@addOnCompleteListener
                }

                // Token novo recebido
                val token = task.result
                println("FCM: Token Gerado com Sucesso: $token")

                // --- ALTERAÇÃO PRINCIPAL AQUI ---
                // Guardar SEMPRE o token localmente, esteja logado ou não.
                SettingsManager.saveFcmToken(token)
                // --------------------------------

                // Se já estiver logado, envia já.
                if (SessionManager.isLoggedInNow()) {
                    println("FCM: Utilizador já logado, a enviar token para backend...")
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            ApiClient.notificationRepository.registerDevice(token)
                            println("FCM: Enviado para o backend com sucesso!")
                        } catch (e: Exception) {
                            println("FCM: Erro ao enviar para API: ${e.message}")
                        }
                    }
                } else {
                    println("FCM: Utilizador NÃO logado. Token guardado no SettingsManager para envio pós-login.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            // Atualizar a referência da Activity para o MSAL funcionar
            msAuthManager.currentActivity = this

            SasIpcaTheme {
                ObserveScreenSize()
                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::msAuthManager.isInitialized) {
            msAuthManager.currentActivity = null
        }
    }
}