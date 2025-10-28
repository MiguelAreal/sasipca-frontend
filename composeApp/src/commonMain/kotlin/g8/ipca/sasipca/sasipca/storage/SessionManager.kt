package g8.ipca.sasipca.sasipca.storage

import com.russhwolf.settings.Settings

object SessionManager {
    private lateinit var settings: Settings

    fun init(settingsInstance: Settings) {
        settings = settingsInstance
    }

    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    fun saveSession(token: String, userId: Int, userName: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
        settings.putInt(KEY_USER_ID, userId)
        settings.putString(KEY_USER_NAME, userName)
    }

    fun getAuthToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)

    fun isLoggedIn(): Boolean = getAuthToken() != null

    fun clearSession() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
    }

    val currentUserID: String? get() = settings.getStringOrNull(KEY_USER_ID)
    val currentUserName: String? get() = settings.getStringOrNull(KEY_USER_NAME)
}
