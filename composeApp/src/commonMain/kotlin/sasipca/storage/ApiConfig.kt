package sasipca.storage

object ApiConfig {
    fun baseUrl(): String = "https://${SettingsManager.getServerIp()}/api"
}
