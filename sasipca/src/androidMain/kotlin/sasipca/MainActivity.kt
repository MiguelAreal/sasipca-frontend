package sasipca

import android.Manifest
import android.content.Intent
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
        SettingsManager.init(settings)

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

                // Guardar SEMPRE o token localmente
                SettingsManager.saveFcmToken(token)

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

        // 5. Verificar se viemos do Widget (Intent)
        // Fazemos isto APÓS as inicializações para garantir que o ‘App’ tem tudo pronto
        val openCalendarFromWidget = intent?.extras?.getBoolean("OPEN_CALENDAR") == true

        setContent {
            // Atualizar a referência da Activity para o MSAL funcionar
            msAuthManager.currentActivity = this

            SasIpcaTheme {
                ObserveScreenSize()
                // Passamos a flag para o App
                App(openCalendar = openCalendarFromWidget)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::msAuthManager.isInitialized) {
            msAuthManager.currentActivity = null
        }
    }

    // Chamado se a app já estiver em background e clicarmos no Widget
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Atualiza o intent da activity com o novo (para getIntent() funcionar se necessário)
        setIntent(intent)

        val openCalendar = intent.extras?.getBoolean("OPEN_CALENDAR") == true

        if (openCalendar) {
            // Reinicia a UI com a ‘flag’ ativa para forçar a navegação
            setContent {
                if (::msAuthManager.isInitialized) {
                    msAuthManager.currentActivity = this
                }
                SasIpcaTheme {
                    ObserveScreenSize()
                    App(openCalendar = true)
                }
            }
        }
    }
}