package sasipca.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sasipca.network.ApiClient // Importar ApiClient
import sasipca.repositories.AuthRepository
import sasipca.screens.navigation.SettingsScreen
import sasipca.storage.NotificationManager
import sasipca.utils.SnackbarManager
import sasipca.models.SnackbarType
import sasipca_app.sasipca.generated.resources.Res
import sasipca_app.sasipca.generated.resources.login_bg
import sasipca_app.sasipca.generated.resources.logo_white

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val navigator = LocalNavigator.currentOrThrow

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF24804F))) {

        @OptIn(ExperimentalResourceApi::class)
        Image(
            painter = painterResource(Res.drawable.login_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.15f }
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                @OptIn(ExperimentalResourceApi::class)
                Image(
                    painter = painterResource(Res.drawable.logo_white),
                    contentDescription = "IPCA Logo",
                    modifier = Modifier.height(150.dp).padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
                Text("Controlo de stock com facilidade.", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 20.dp)) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isLoading) return@launch
                            isLoading = true

                            // 1. Tenta Login
                            val loginResult = authRepository.loginMicrosoft()

                            loginResult.fold(
                                onSuccess = {
                                    // 2. Login OK -> Tenta carregar listas
                                    try {
                                        ApiClient.listsRepository.loadLists()
                                        NotificationManager.refreshCount()
                                        // 3. Tudo OK -> Navega
                                        onLoginSuccess()
                                    } catch (e: Exception) {
                                        // Falha ao carregar listas (mesmo com login ok)
                                        isLoading = false
                                        SnackbarManager.show("Login efetuado, mas falha ao carregar dados: ${e.message}", SnackbarType.ERROR)
                                        // Opcional: fazer logout se os dados forem críticos
                                        // authRepository.logout()
                                    }
                                },
                                onFailure = {
                                    isLoading = false
                                    SnackbarManager.show("Falha na autenticação: ${it.message}", SnackbarType.ERROR)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF24804F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.widthIn(280.dp, 400.dp).height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF24804F), strokeWidth = 2.dp)
                    } else {
                        Text("Entrar com conta IPCA", fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Utilize as suas credenciais institucionais", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        IconButton(
            onClick = { navigator.push(SettingsScreen()) },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding().size(36.dp).background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.Settings, "Definições", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}