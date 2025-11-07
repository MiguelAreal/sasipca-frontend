package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import g8.ipca.sasipca.sasipca.navigation.NavigationService
import g8.ipca.sasipca.sasipca.navigation.Screen
import g8.ipca.sasipca.sasipca.repositories.*
import g8.ipca.sasipca.sasipca.utils.SnackbarManager
import g8.ipca.sasipca.sasipca.utils.SnackbarType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sasipca.composeapp.generated.resources.Res
import sasipca.composeapp.generated.resources.login_bg
import sasipca.composeapp.generated.resources.logo


@Composable
fun LoginScreen(authRepository: AuthRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF24804F))
    ) {
        // Background Image

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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(280.dp, 400.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                @OptIn(ExperimentalResourceApi::class)
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "IPCA Logo",
                    modifier = Modifier
                        .sizeIn(130.dp, 180.dp, 150.dp, 200.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Controlo de stock com facilidade.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("E-Mail") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Palavra-Passe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Esconder Palavra-Passe" else "Mostrar Palavra-Passe",
                                tint = Color.Gray
                            )
                        }
                    }
                )

                // Forgot password
                TextButton(
                    onClick = { /* Handle forgot password */ },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = "Esqueceu-se da palavra-passe?",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Start,
                        textDecoration = TextDecoration.Underline,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                tint = Color.White
            )
        }


        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    val result = authRepository.login(email, password)
                    result.fold(
                        onSuccess = {
                            NavigationService.resetTo(Screen.Main)
                        },
                        onFailure = {
                            SnackbarManager.show("Erro no login: ${it.message}", SnackbarType.ERROR)
                        }
                    )
                    isLoading = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF),      // Background color
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(300.dp, 400.dp)
                .padding(24.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFFFFFFF),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Entrar")
            }
        }

    }
}