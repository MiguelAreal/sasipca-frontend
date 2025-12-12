package sasipca.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsManager {
    private lateinit var settings: Settings

    // --- ESTADO REATIVO DO TEMA ---
    private val _isDarkThemeFlow = MutableStateFlow(false)
    val isDarkThemeFlow: StateFlow<Boolean> = _isDarkThemeFlow.asStateFlow()

    fun init(settingsInstance: Settings) {
        settings = settingsInstance
        // Carrega o valor inicial da persistência para o fluxo
        _isDarkThemeFlow.value = settings.getBoolean(KEY_DARK_THEME, false)
    }

    // Keys
    private const val KEY_SERVER_IP = "server_ip"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val DEFAULT_SERVER_IP = "rapi.tail1fcae6.ts.net"

    // Server IP
    fun getServerIp(): String {
        // Verifica se settings foi inicializado para evitar crash em previews ou testes
        if (!::settings.isInitialized) return DEFAULT_SERVER_IP
        return settings.getStringOrNull(KEY_SERVER_IP) ?: DEFAULT_SERVER_IP
    }

    fun setServerIp(ip: String) {
        if (::settings.isInitialized) settings.putString(KEY_SERVER_IP, ip)
    }


    fun setDarkTheme(isDark: Boolean) {
        if (::settings.isInitialized) {
            settings.putBoolean(KEY_DARK_THEME, isDark)
            // IMPORTANTE: Atualiza o fluxo para notificar quem estiver a ouvir (App.kt)
            _isDarkThemeFlow.value = isDark
        }
    }

    // Reset to defaults
    fun resetToDefaults() {
        if (::settings.isInitialized) {
            settings.putString(KEY_SERVER_IP, DEFAULT_SERVER_IP)
            setDarkTheme(false) // Isto já atualiza o fluxo
        }
    }
}