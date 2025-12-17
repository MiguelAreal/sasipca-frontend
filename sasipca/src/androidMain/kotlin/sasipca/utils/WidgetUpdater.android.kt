package sasipca.utils

import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.datastore.preferences.core.booleanPreferencesKey // <--- Adicionar
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sasipca.storage.SessionManager
import sasipca.widget.CalendarWidget

actual fun updateWidgets() {
    val context = AndroidContext.get()

    if (context != null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Pequeno delay para garantir persistência (IO)
                delay(500)

                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(CalendarWidget::class.java)

                // Definir chaves
                val updateKey = longPreferencesKey("last_update_timestamp")
                val loggedInKey = booleanPreferencesKey("is_logged_in") // <--- Nova chave
                val adminKey = booleanPreferencesKey("is_admin")       // <--- Nova chave

                // Capturar estado ATUAL da App (Fonte da verdade)
                val isUserLoggedIn = SessionManager.isLoggedInNow()
                val isUserAdmin = SessionManager.isAdmin()

                glanceIds.forEach { glanceId ->
                    // 1. Injetar o estado da App nas preferências do Widget
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[updateKey] = System.currentTimeMillis()
                        prefs[loggedInKey] = isUserLoggedIn
                        prefs[adminKey] = isUserAdmin
                    }
                    // 2. Forçar atualização visual
                    CalendarWidget().update(context, glanceId)
                }
                println("WidgetUpdater: Widgets atualizados. LoggedIn: $isUserLoggedIn")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}