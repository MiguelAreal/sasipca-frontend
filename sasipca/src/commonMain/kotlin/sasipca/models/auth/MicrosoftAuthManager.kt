package sasipca.models.auth

interface MicrosoftAuthManager {
    suspend fun signIn(): String? // Retorna o idToken ou null se falhar
    suspend fun signOut()
}