package g8.ipca.sasipca.sasipca

import LoginScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import g8.ipca.sasipca.sasipca.network.ApiClient
import g8.ipca.sasipca.sasipca.repositories.AuthRepository

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


@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = CustomTypography,
        content = content
    )
}

@Composable
fun App() {
    val authRepository = remember { AuthRepository(ApiClient.client) }
    AppTheme {
        LoginScreen(authRepository = authRepository)
    }
}
