package sasipca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import sasipca_app.sasipca.generated.resources.Res
import sasipca_app.sasipca.generated.resources.plusjakartasans
import sasipca_app.sasipca.generated.resources.plusjakartasans_italic

// -------------------------------
// PALETA DE CORES IPCA
// -------------------------------
val IPCAGreen = Color(0xFF24804F)           // Verde IPCA principal
private val IPCAGreenDark = Color(0xFF15492F)       // Verde-escuro IPCA
private val IPCAGreenLight = Color(0xFF3FA66D)      // Verde claro
private val IPCAGreenLighter = Color(0xFFE8F5E9)    // Verde muito claro (formações de base)

private val IPCAGray = Color(0xFF6B7280)            // Cinza neutro
private val IPCAGrayLight = Color(0xFFF3F4F6)       // Cinza claro

// -------------------------------
// LIGHT COLOR SCHEME
// -------------------------------
private val LightColorScheme = lightColorScheme(
    primary = IPCAGreen,
    onPrimary = Color.White,
    primaryContainer = IPCAGreenLighter,
    onPrimaryContainer = IPCAGreenDark,

    secondary = IPCAGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E7DD),
    onSecondaryContainer = Color(0xFF0F3B24),

    tertiary = IPCAGreenLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8E6C9),
    onTertiaryContainer = Color(0xFF0A3D1F),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = IPCAGrayLight,
    onSurfaceVariant = IPCAGray,

    surfaceTint = IPCAGreen,
    inverseSurface = Color(0xFF2F2F2F),
    inverseOnSurface = Color(0xFFF0F0F0),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    outline = Color(0xFFCACACA),
    outlineVariant = Color(0xFFE0E0E0),
    scrim = Color.Black,
)

// -------------------------------
// DARK COLOR SCHEME
// -------------------------------
private val DarkColorScheme = darkColorScheme(
    primary = IPCAGreenLight,
    onPrimary = Color(0xFF003919),
    primaryContainer = IPCAGreenDark,
    onPrimaryContainer = Color(0xFFB8E6C9),

    secondary = Color(0xFF81C995),
    onSecondary = Color(0xFF003919),
    secondaryContainer = Color(0xFF1A5F37),
    onSecondaryContainer = Color(0xFFD1E7DD),

    tertiary = Color(0xFF9DD9B0),
    onTertiary = Color(0xFF00391A),
    tertiaryContainer = Color(0xFF0F5C2E),
    onTertiaryContainer = Color(0xFFE8F5E9),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E6E6),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCACACA),

    surfaceTint = IPCAGreenLight,
    inverseSurface = Color(0xFFE6E6E6),
    inverseOnSurface = Color(0xFF1E1E1E),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    outline = Color(0xFF4A4A4A),
    outlineVariant = Color(0xFF3A3A3A),
    scrim = Color.Black,
)

// -------------------------------
// TIPOGRAFIA PERSONALIZADA
// -------------------------------
@Composable
fun customFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.plusjakartasans, weight = FontWeight.Normal),
        Font(Res.font.plusjakartasans_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
    )
}

@Composable
fun customTypography(): Typography {
    val fontFamily = customFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 57.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 64.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 52.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 44.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,
            fontStyle = FontStyle.Italic,
            lineHeight = 16.sp
        )
    )
}

// -------------------------------
// TEMA PRINCIPAL SASIPCA
// -------------------------------
@Composable
fun SasIpcaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = customTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}