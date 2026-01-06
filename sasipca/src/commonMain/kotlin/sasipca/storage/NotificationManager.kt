package sasipca.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import sasipca.network.ApiClient

object NotificationManager {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Estado do Distintivo (Bolinha vermelha)
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    // Gatilho para recarregar a LISTA no ecrã
    private val _reloadTrigger = MutableSharedFlow<Unit>()
    val reloadTrigger: SharedFlow<Unit> = _reloadTrigger.asSharedFlow()

    // Chamado no arranque e quando chega Push
    fun refreshCount() {
        scope.launch {
            val count = ApiClient.notificationRepository.getUnreadCount()
            _unreadCount.value = count
            // Avisa quem estiver a ouvir (o ecrã de notificações) para recarregar a lista
            _reloadTrigger.emit(Unit)
        }
    }

    fun decrementCount() {
        if (_unreadCount.value > 0) {
            _unreadCount.value -= 1
        }
    }
}