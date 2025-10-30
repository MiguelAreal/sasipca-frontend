package g8.ipca.sasipca.sasipca.storage

import com.russhwolf.settings.Settings

object SettingsManager {
    private lateinit var settings: Settings

    fun init(settingsInstance: Settings) {
        settings = settingsInstance
    }

    // Keys
    private const val KEY_SERVER_IP = "server_ip"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val DEFAULT_SERVER_IP = "192.168.1.100"

    // Server IP
    fun getServerIp(): String {
        return settings.getStringOrNull(KEY_SERVER_IP) ?: DEFAULT_SERVER_IP
    }

    fun setServerIp(ip: String) {
        settings.putString(KEY_SERVER_IP, ip)
    }

    // Theme
    fun isDarkTheme(): Boolean {
        return settings.getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkTheme(isDark: Boolean) {
        settings.putBoolean(KEY_DARK_THEME, isDark)
    }

    // Reset to defaults
    fun resetToDefaults() {
        settings.putString(KEY_SERVER_IP, DEFAULT_SERVER_IP)
        settings.putBoolean(KEY_DARK_THEME, false)
    }
}