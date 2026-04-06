package com.swiftcart.minimlist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.swiftcart.minimlist.PreferenceManager

@Composable
fun MinimlistTheme(
    prefs: PreferenceManager? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeMode = prefs?.getThemeMode() ?: "system"
    val multiplier = prefs?.getTextSizeMultiplier() ?: 1.0f
    val fontStyle = prefs?.getFontStyle() ?: "Default"

    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> darkTheme
    }

    // Strict Black, White, and Grey palette
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            secondary = Color(0xFF757575), // Medium Grey
            onSecondary = Color.White,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color(0xFF121212), // Very Dark Grey
            onSurface = Color.White,
            surfaceVariant = Color(0xFF212121), // Dark Grey
            onSurfaceVariant = Color.LightGray,
            outline = Color(0xFF424242) // Grey Outline
        )
    } else {
        lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            secondary = Color(0xFF757575), // Medium Grey
            onSecondary = Color.Black,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color(0xFFF5F5F5), // Very Light Grey
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFEEEEEE), // Light Grey
            onSurfaceVariant = Color.DarkGray,
            outline = Color(0xFFBDBDBD) // Light Grey Outline
        )
    }

    val fontFamily = when (fontStyle) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }

    val baseTypography = Typography()
    val customTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = (baseTypography.displayLarge.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontSize = (baseTypography.displayMedium.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontSize = (baseTypography.displaySmall.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = (baseTypography.headlineLarge.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = (baseTypography.headlineMedium.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = (baseTypography.headlineSmall.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontSize = (baseTypography.titleLarge.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontSize = (baseTypography.titleMedium.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontSize = (baseTypography.titleSmall.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = (baseTypography.bodyLarge.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = (baseTypography.bodyMedium.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontSize = (baseTypography.bodySmall.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontSize = (baseTypography.labelLarge.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontSize = (baseTypography.labelMedium.fontSize.value * multiplier).sp, fontFamily = fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontSize = (baseTypography.labelSmall.fontSize.value * multiplier).sp, fontFamily = fontFamily),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
