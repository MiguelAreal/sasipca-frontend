package sasipca.network

import sasipca.storage.SettingsManager

object ApiConfig {
    fun baseUrl(): String = "https://${SettingsManager.getServerIp()}/api"
}
