package sasipca.auth

import com.microsoft.aad.msal4j.*
import java.net.URI
import java.util.Collections

class MicrosoftAuthManagerDesktop : MicrosoftAuthManager {

    private val CLIENT_ID = "1047dc49-3d68-457b-a0d4-ecc75ed581eb"
    private val AUTHORITY = "https://login.microsoftonline.com/common/"
    private val REDIRECT_URI = "http://localhost" // Importante configurar no Azure!

    override suspend fun signIn(): String? {
        return try {
            val app = PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY)
                .build()

            val parameters = InteractiveRequestParameters.builder(URI(REDIRECT_URI))
                .scopes(Collections.singleton("User.Read"))
                .build()

            // Isto vai abrir o browser do sistema automaticamente
            val result: IAuthenticationResult = app.acquireToken(parameters).get()

            // O idToken vem dentro do result.idToken()
            result.idToken()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun signOut() {
        // MSAL4J desktop não gere sessão global da mesma forma, 
        // mas podes limpar a cache de tokens se implementares persistência.
    }
}