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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sasipca.composeapp.generated.resources.Res
import sasipca.composeapp.generated.resources.login_bg
import sasipca.composeapp.generated.resources.logo_white
import sasipca.navigation.NavigationService
import sasipca.navigation.Screen
import sasipca.repositories.AuthRepository
import sasipca.utils.SnackbarManager
import sasipca.models.SnackbarType

@Composable
fun LoginScreen(authRepository: AuthRepository) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF24804F))
    ) {

        @OptIn(ExperimentalResourceApi::class)
        Image(
            painter = painterResource(Res.drawable.login_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.15f }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                @OptIn(ExperimentalResourceApi::class)
                Image(
                    painter = painterResource(Res.drawable.logo_white),
                    contentDescription = "IPCA Logo",
                    modifier = Modifier
                        .height(150.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Controlo de stock com facilidade.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isLoading) return@launch
                            isLoading = true
                            val result = authRepository.loginMicrosoft()
                            result.fold(
                                onSuccess = { NavigationService.resetTo(Screen.Main) },
                                onFailure = {
                                    isLoading = false
                                    SnackbarManager.show("Falha na autenticação: ${it.message}", SnackbarType.ERROR)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF24804F),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .widthIn(280.dp, 400.dp)
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF24804F),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Entrar com conta IPCA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Utilize as suas credenciais institucionais",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp, // Tamanho fixo
                    textAlign = TextAlign.Center
                )
            }
        }


        IconButton(
            onClick = { NavigationService.navigateTo(Screen.Settings) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Definições",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}