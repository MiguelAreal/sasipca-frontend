package g8.ipca.sasipca.sasipca

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.material3.Typography
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.repositories.AuthRepository
import g8.ipca.sasipca.sasipca.screens.*
import g8.ipca.sasipca.sasipca.ui.components.CustomSnackbarHost
import g8.ipca.sasipca.sasipca.ui.components.SnackbarMessage
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import g8.ipca.sasipca.sasipca.ui.components.SnackbarManager


val CustomFontFamily = FontFamily(
    Font(resource = "fonts/PlusJakartaSans.ttf", weight = FontWeight.Normal),
    Font(resource = "fonts/PlusJakartaSans-Italic.ttf", weight = FontWeight.Normal, style = androidx.compose.ui.text.font.FontStyle.Italic)
)
val CustomTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = TextStyle(
        fontFamily = CustomFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Light,
        fontStyle = FontStyle.Italic
    )
)

enum class Screens {
    LOGIN,
    DASHBOARD
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = CustomTypography,
        content = content
    )
}

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    val authRepository = remember { AuthRepository(ApiClient.client) }
    var currentScreen by remember { mutableStateOf(Screens.LOGIN) }

    val snackbarState = remember { mutableStateOf<SnackbarMessage?>(null) }
    val scope = rememberCoroutineScope()

    // Inicializa o gestor global
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarState = snackbarState
        SnackbarManager.scope = scope
    }

    AppTheme {
        Scaffold { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {

                // Conteúdo principal
                when (currentScreen) {
                    Screens.LOGIN -> LoginScreen(
                        authRepository = authRepository,
                        onLoginSuccess = { currentScreen = Screens.DASHBOARD }
                    )
                    Screens.DASHBOARD -> DashboardScreen()
                }

                CustomSnackbarHost(
                    snackbarMessageState = snackbarState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )

            }
        }
    }

}