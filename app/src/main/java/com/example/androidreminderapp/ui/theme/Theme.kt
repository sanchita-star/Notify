package com.example.androidreminderapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE448D1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF641B70),
    onPrimaryContainer = Color(0xFFFFD6F8),
    secondary = Color(0xFFD8B8E8),
    onSecondary = Color(0xFF24122E),
    secondaryContainer = Color(0xFF3B2448),
    onSecondaryContainer = Color(0xFFF4DDF2),
    tertiary = Color(0xFFC9A0FF),
    onTertiary = Color(0xFF25113A),
    background = PageDark,
    onBackground = Color(0xFFFFF7FF),
    surface = SurfaceDark,
    onSurface = Color(0xFFFFF7FF),
    surfaceVariant = Color(0xFF30213C),
    onSurfaceVariant = Color(0xFFD0BED8),
    outline = Color(0xFF9B80A5),
    outlineVariant = Color(0xFF49374F),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF5C2428),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB91AA4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF9D7F2),
    onPrimaryContainer = Color(0xFF520B48),
    secondary = Color(0xFF69536C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0DDF1),
    onSecondaryContainer = Color(0xFF34243A),
    tertiary = Color(0xFF695083),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEEDCFF),
    onTertiaryContainer = Color(0xFF35204B),
    background = Color(0xFFF8F0FA),
    onBackground = Color(0xFF25182B),
    surface = Color(0xFFFFFAFF),
    onSurface = Color(0xFF25182B),
    surfaceVariant = Color(0xFFEFE3F2),
    onSurfaceVariant = Color(0xFF625566),
    outline = Color(0xFF77857D),
    outlineVariant = OutlineSoft
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
)

@Composable
fun AndroidReminderAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
