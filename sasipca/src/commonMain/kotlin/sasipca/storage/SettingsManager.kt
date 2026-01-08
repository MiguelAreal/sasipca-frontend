package sasipca.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsManager {
    private lateinit var settings: Settings

    // Keys
    private const val KEY_SERVER_IP = "server_ip"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_FCM_TOKEN = "fcm_token"

    private const val DEFAULT_SERVER_IP = "sasipca.rapi4real.duckdns.org"

    // --- ESTADO REATIVO DO TEMA ---
    private val _isDarkThemeFlow = MutableStateFlow(false)
    val isDarkThemeFlow: StateFlow<Boolean> = _isDarkThemeFlow.asStateFlow()

    fun init(settingsInstance: Settings) {
        settings = settingsInstance
        // Carrega o valor inicial da persistência para o fluxo
        _isDarkThemeFlow.value = settings.getBoolean(KEY_DARK_THEME, false)
    }

    // Server IP
    fun getServerIp(): String {
        if (!::settings.isInitialized) return DEFAULT_SERVER_IP
        return settings.getStringOrNull(KEY_SERVER_IP) ?: DEFAULT_SERVER_IP
    }

    fun setServerIp(ip: String) {
        if (::settings.isInitialized) settings.putString(KEY_SERVER_IP, ip)
    }

    // Theme
    fun setDarkTheme(isDark: Boolean) {
        if (::settings.isInitialized) {
            settings.putBoolean(KEY_DARK_THEME, isDark)
            _isDarkThemeFlow.value = isDark
        }
    }

    // --- GESTÃO TOKEN FCM (NOVO) ---
    fun saveFcmToken(token: String) {
        if (::settings.isInitialized) {
            settings.putString(KEY_FCM_TOKEN, token)
        }
    }

    fun getFcmToken(): String? {
        if (!::settings.isInitialized) return null
        return settings.getStringOrNull(KEY_FCM_TOKEN)
    }
    // -------------------------------

}