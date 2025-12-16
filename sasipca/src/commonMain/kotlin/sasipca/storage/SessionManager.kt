package sasipca.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private lateinit var settings: Settings

    // Keys
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ROLE = "user_role" // NOVO

    // --- ESTADO REATIVO DE SESSÃO ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- EVENTO DE LOGOUT FORÇADO ---
    // Usado pelo Network Layer para avisar a UI que o token expirou
    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent = _logoutEvent.asSharedFlow()

    fun init(settingsInstance: Settings) {
        settings = settingsInstance

        // Inicializar estado de login baseado se temos token guardado
        if (settings.getStringOrNull(KEY_ACCESS_TOKEN) != null) {
            _isLoggedIn.value = true
        }
    }

    /**
     * Guarda dados do utilizador localmente
     */
    fun saveSession(
        token: String,
        refreshToken: String,
        userID: Int,
        userName: String,
        role: String
    ) {
        if (!::settings.isInitialized) return

        settings.putString(KEY_ACCESS_TOKEN, token)
        settings.putString(KEY_REFRESH_TOKEN, refreshToken)
        settings.putInt(KEY_USER_ID, userID)
        settings.putString(KEY_USER_NAME, userName)
        settings.putString(KEY_USER_ROLE, role)

        _isLoggedIn.value = true
    }

    // Getters seguros
    fun getAccessToken(): String? {
        if (!::settings.isInitialized) return null
        return settings.getStringOrNull(KEY_ACCESS_TOKEN)
    }

    fun getRefreshToken(): String? {
        if (!::settings.isInitialized) return null
        return settings.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    fun getUserName(): String? {
        if (!::settings.isInitialized) return null
        return settings.getStringOrNull(KEY_USER_NAME)
    }

    fun getUserId(): Int? {
        if (!::settings.isInitialized) return null
        return settings.getIntOrNull(KEY_USER_ID)
    }

    fun getUserRole(): String? {
        if (!::settings.isInitialized) return null
        return settings.getStringOrNull(KEY_USER_ROLE)
    }

    fun isAdmin(): Boolean {
        if (!::settings.isInitialized) return false
        return settings.getStringOrNull(KEY_USER_ROLE).equals("Admin", ignoreCase = true)
    }

    fun setAccessToken(newToken: String) {
        if (::settings.isInitialized) {
            settings.putString(KEY_ACCESS_TOKEN, newToken)
        }
    }

    /**
     * Verifica atualmente se o utilizador está com sessão iniciada.
     */
    fun isLoggedInNow(): Boolean = _isLoggedIn.value

    /**
     * Dispara um logout forçado (ex: Refresh token expirado).
     * Notifica a UI via Flow.
     */
    suspend fun triggerLogout() {
        clear()
        _logoutEvent.emit(Unit)
    }

    /**
     * Limpa dados de sessão e marca utilizador como sessão terminada.
     */
    fun clear() {
        if (!::settings.isInitialized) return

        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_ROLE) // Limpa o role

        _isLoggedIn.value = false
    }
}