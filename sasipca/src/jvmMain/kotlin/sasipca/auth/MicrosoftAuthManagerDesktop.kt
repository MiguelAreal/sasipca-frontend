package sasipca.auth

import com.microsoft.aad.msal4j.*
import java.net.URI
import java.util.Collections

class MicrosoftAuthManagerDesktop : MicrosoftAuthManager {

    private val clientId = "1047dc49-3d68-457b-a0d4-ecc75ed581eb"
    private val authority = "https://login.microsoftonline.com/common/"
    private val redirectUrl = "http://localhost" // Importante configurar no Azure!

    override suspend fun signIn(): String? {
        return try {
            val app = PublicClientApplication.builder(clientId)
                .authority(authority)
                .build()

            val parameters = InteractiveRequestParameters.builder(URI(redirectUrl))
                .scopes(Collections.singleton("User.Read"))
                .build()

            // Isto vai abrir o navegador do sistema automaticamente
            val result: IAuthenticationResult = app.acquireToken(parameters).get()

            // O idToken vem dentro do result.idToken()
            result.idToken()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun signOut() {
        // Não faz nada em ‘Desktop’
    }
}