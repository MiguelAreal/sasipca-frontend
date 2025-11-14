package sasipca.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import sasipca.navigation.NavigationService
import kotlin.time.ExperimentalTime

object SessionManager {
    private lateinit var settings: Settings

    /**
     * Estado de sessão reativo (true se logado, falso se não)
     */
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    fun init(settingsInstance: Settings) {
        settings = settingsInstance

        // Inicializar estado de login baseado em dados de sessão guardados
        _isLoggedIn.value = settings.getStringOrNull("access_token") != null &&
                settings.getStringOrNull("user_name") != null
    }

    /**
     * Guarda dados do utilizador localmente
     */
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
        _isLoggedIn.value = true
    }

    fun getAccessToken(): String? = settings.getStringOrNull("access_token")
    fun getRefreshToken(): String? = settings.getStringOrNull("refresh_token")
    fun getUserName(): String? = settings.getStringOrNull("user_name")
    fun setAccessToken(newToken: String) {
        settings.putString("access_token", newToken)
    }

    /**
     * Verifica atualmente se o utilizador está com sessão iniciada.
     */
    fun isLoggedInNow(): Boolean = _isLoggedIn.value

    /**
     * Limpa dados de sessão e marca utilizador como sessão terminada.
     */
    fun clear() {
        settings.remove("access_token")
        settings.remove("refresh_token")
        settings.remove("user_id")
        settings.remove("user_name")
        _isLoggedIn.value = false

        //Aproveita e faz reset aos separadores
        NavigationService.resetMainTabIndex()
    }
}
