package g8.ipca.sasipca.sasipca.storage

import g8.ipca.sasipca.sasipca.storage.SettingsManager

object ApiConfig {
    fun baseUrl(): String = "https://${SettingsManager.getServerIp()}/api"
}
