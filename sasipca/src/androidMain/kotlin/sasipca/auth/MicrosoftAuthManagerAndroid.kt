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

        if (app == null || activity == null) {
            Log.e("MSAL_DEBUG", "Erro: App ou Activity nulos.")
            continuation.resume(null)
            return@suspendCoroutine
        }

        // Tenta primeiro "silent"
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
                        Log.d("MSAL_DEBUG", "Silent Login: Falhou (${exc.message}). Limpando sessão anterior...")

                        // CORREÇÃO: Forçar Logout antes de tentar Login Interativo
                        // Isto resolve o erro "An account is already signed in"
                        app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                            override fun onSignOut() {
                                Log.d("MSAL_DEBUG", "Sessão antiga limpa. A iniciar login interativo...")
                                startInteractiveLogin(app, activity, continuation)
                            }

                            override fun onError(exception: MsalException) {
                                // Se der erro no logout (ex: não havia conta), prossegue para login na mesma
                                Log.w("MSAL_DEBUG", "Erro ao limpar sessão antiga (ignorável): ${exception.message}")
                                startInteractiveLogin(app, activity, continuation)
                            }
                        })
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

    override suspend fun signOut() = suspendCoroutine { continuation ->
        val app = mSingleAccountApp

        if (app == null) {
            continuation.resume(Unit)
            return@suspendCoroutine
        }

        app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                Log.d("MSAL_DEBUG", "MSAL Logout: Sucesso. Cache limpa.")
                continuation.resume(Unit)
            }

            override fun onError(exception: MsalException) {
                Log.e("MSAL_DEBUG", "MSAL Logout: Erro - ${exception.message}")
                // Mesmo com erro, continuamos o fluxo para não prender o utilizador
                continuation.resume(Unit)
            }
        })
    }
}