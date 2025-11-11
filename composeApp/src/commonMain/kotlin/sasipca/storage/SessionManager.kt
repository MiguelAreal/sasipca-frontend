package sasipca.storage

import com.russhwolf.settings.Settings
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object SessionManager {
    private lateinit var settings: Settings

    fun init(settingsInstance: Settings) {
        settings = settingsInstance
    }

    @OptIn(ExperimentalTime::class)
    fun saveSession(
        token: String,
        refreshToken: String,
        userID: Int,
        userName: String
    ) {
        settings.putString("access_token", token)
        settings.putString("refresh_token", refreshToken)
        settings.putInt("user_id", userID)
        settings.putString("user_name", userName)
    }

    fun getAccessToken(): String? = settings.getStringOrNull("access_token")
    fun getRefreshToken(): String? = settings.getStringOrNull("refresh_token")
    fun getUserId(): Int? = settings.getIntOrNull("user_id")
    fun getUserName(): String? = settings.getStringOrNull("user_name")
    fun setAccessToken(newToken: String) {
        settings.putString("access_token", newToken)
    }
    fun setRefreshToken(newToken: String) {
        settings.putString("refresh_token", newToken)
    }

    // Elimina apenas dados relacionados à conta.
    fun clear() {
        settings.remove("access_token")
        settings.remove("refresh_token")
        settings.remove("user_id")
        settings.remove("user_name")
    }
}
