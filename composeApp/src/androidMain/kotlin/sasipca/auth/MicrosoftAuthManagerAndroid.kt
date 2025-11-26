package sasipca.auth

import android.app.Activity
import android.content.Context
import android.util.Log // Importante para ver o erro
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import app.sasipca.R
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MicrosoftAuthManagerAndroid(private val context: Context) : MicrosoftAuthManager {

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null

    suspend fun init() = suspendCoroutine { continuation ->
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.auth_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    Log.d("MSAL_DEBUG", "Inicialização MSAL: SUCESSO")
                    mSingleAccountApp = application
                    continuation.resume(Unit)
                }
                override fun onError(exception: MsalException) {
                    // AQUI ESTÁ O SEGREDO: Vamos ver qual é o erro
                    Log.e("MSAL_DEBUG", "Inicialização MSAL FALHOU: ${exception.message}", exception)
                    continuation.resume(Unit)
                }
            }
        )
    }

    var currentActivity: Activity? = null

    override suspend fun signIn(): String? = suspendCoroutine { continuation ->
        val app = mSingleAccountApp
        val activity = currentActivity

        // Log para diagnóstico
        if (app == null) {
            Log.e("MSAL_DEBUG", "Erro: mSingleAccountApp é NULL. O init falhou antes.")
            continuation.resume(null)
            return@suspendCoroutine
        }
        if (activity == null) {
            Log.e("MSAL_DEBUG", "Erro: currentActivity é NULL.")
            continuation.resume(null)
            return@suspendCoroutine
        }

        app.acquireTokenSilentAsync(
            AcquireTokenSilentParameters.Builder()
                .withScopes(listOf("User.Read"))
                .fromAuthority(app.configuration.defaultAuthority.authorityURL.toString())
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        Log.d("MSAL_DEBUG", "Silent Login: Sucesso")
                        continuation.resume(result.account.idToken)
                    }
                    override fun onError(exc: MsalException) {
                        Log.d("MSAL_DEBUG", "Silent Login: Falhou, a tentar interativo...")
                        startInteractiveLogin(app, activity, continuation)
                    }
                    override fun onCancel() { continuation.resume(null) }
                }).build()
        )
    }

    private fun startInteractiveLogin(
        app: ISingleAccountPublicClientApplication,
        activity: Activity,
        continuation: kotlin.coroutines.Continuation<String?>
    ) {
        app.signIn(
            SignInParameters.builder()
                .withActivity(activity)
                .withScopes(listOf("User.Read"))
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        Log.d("MSAL_DEBUG", "Interactive Login: Sucesso")
                        continuation.resume(result.account.idToken)
                    }
                    override fun onError(exception: MsalException) {
                        Log.e("MSAL_DEBUG", "Interactive Login: Erro - ${exception.message}")
                        continuation.resume(null)
                    }
                    override fun onCancel() {
                        Log.d("MSAL_DEBUG", "Interactive Login: Cancelado")
                        continuation.resume(null)
                    }
                }).build()
        )
    }

    // ... manter signOut igual ...
    override suspend fun signOut() {
        mSingleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {}
            override fun onError(exception: MsalException) {}
        })
    }
}