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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.VisualTransformation
import g8.ipca.sasipca.sasipca.repositories.*
import kotlinx.coroutines.launch



@Composable
fun LoginScreen(authRepository: AuthRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4A6FA5))
    ) {
        // Background Image with 15% opacity
        AsyncImage(
            model = "https://sas.ipca.pt/wp-content/uploads/sites/2/2020/06/1.jpg",
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.15f }
        )

        // Centered scrollable content
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
                // Logo
                Image(
                    painter = painterResource("logo_sas.png"),
                    contentDescription = "IPCA Logo",
                    modifier = Modifier
                        .sizeIn(100.dp, 150.dp, 150.dp, 200.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
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

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Palavra-Passe") },
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
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        null;
                    }
                )

                // Forgot password link
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
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = authRepository.login(email, password)
                            result.fold(
                                onSuccess = { success ->
                                    println("Logged in! userID=${success.userID}, token=${success.token}")
                                    // Navigate or store token
                                },
                                onFailure = { error ->
                                    println("Login failed: ${error.message}")
                                    // Show error to user
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = ButtonDefaults.buttonColors( containerColor = Color(0xFFE8EAF6), contentColor = Color(0xFF4A6FA5) )
                ) {
                    Text("Entrar")
                }
            }


}
