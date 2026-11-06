package ir.revayat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.revayat.app.R

val Gold = Color(0xFFC9A45C)
val GoldSoft = Color(0xFFE3CC93)
val Tulip = Color(0xFFB23A3A)
val Olive = Color(0xFF7E9A55)
val Ink = Color(0xFF0D1310)
val InkSurface = Color(0xFF161F1A)
val InkSurface2 = Color(0xFF1E2923)
val Cream = Color(0xFFF3ECDD)

val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_black, FontWeight.Black),
)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = Olive,
    onSecondary = Ink,
    tertiary = Tulip,
    background = Ink,
    onBackground = Cream,
    surface = InkSurface,
    onSurface = Cream,
    surfaceVariant = InkSurface2,
    onSurfaceVariant = Color(0xFFB9B2A2),
    outline = Color(0xFF3A463F),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8C6A26),
    onPrimary = Color.White,
    secondary = Color(0xFF55703A),
    onSecondary = Color.White,
    tertiary = Tulip,
    background = Color(0xFFF7F2E7),
    onBackground = Color(0xFF1C1A15),
    surface = Color(0xFFFFFBF2),
    onSurface = Color(0xFF1C1A15),
    surfaceVariant = Color(0xFFEDE5D3),
    onSurfaceVariant = Color(0xFF5E584B),
    outline = Color(0xFFD6CCB6),
)

private fun typography(scale: Float): Typography {
    fun s(size: Int, weight: FontWeight, line: Float = 1.6f) = TextStyle(
        fontFamily = Vazir,
        fontWeight = weight,
        fontSize = (size * scale).sp,
        lineHeight = (size * scale * line).sp,
    )
    return Typography(
        displayLarge = s(44, FontWeight.Black, 1.25f),
        displayMedium = s(36, FontWeight.Black, 1.25f),
        displaySmall = s(30, FontWeight.Black, 1.3f),
        headlineLarge = s(28, FontWeight.Bold, 1.35f),
        headlineMedium = s(24, FontWeight.Bold, 1.4f),
        headlineSmall = s(20, FontWeight.Bold, 1.45f),
        titleLarge = s(19, FontWeight.Bold, 1.45f),
        titleMedium = s(16, FontWeight.Bold, 1.5f),
        titleSmall = s(14, FontWeight.Medium, 1.5f),
        bodyLarge = s(16, FontWeight.Normal, 1.9f),
        bodyMedium = s(14, FontWeight.Normal, 1.8f),
        bodySmall = s(12, FontWeight.Normal, 1.7f),
        labelLarge = s(14, FontWeight.Medium),
        labelMedium = s(12, FontWeight.Medium),
        labelSmall = s(11, FontWeight.Medium),
    )
}

@Composable
fun RevayatTheme(themeMode: Int, fontScale: Float, content: @Composable () -> Unit) {
    // 0 = dark (default), 1 = light, 2 = follow system
    val dark = when (themeMode) {
        1 -> false
        2 -> isSystemInDarkTheme()
        else -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = typography(fontScale),
        content = content,
    )
}
