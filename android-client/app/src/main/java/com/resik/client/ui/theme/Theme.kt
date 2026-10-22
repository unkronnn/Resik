package com.resik.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ResikColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = BgWhite,
    primaryContainer = GreenMint,
    onPrimaryContainer = GreenPrimary,
    secondary = OrangeAccent,
    onSecondary = BgWhite,
    secondaryContainer = OrangeLight,
    background = BgLight,
    onBackground = TextPrimary,
    surface = BgWhite,
    onSurface = TextPrimary,
    surfaceVariant = GreenPale,
    outline = BorderLight,
    error = RedLogout,
    onError = BgWhite,
)

@Composable
fun ResikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ResikColorScheme,
        typography = ResikTypography,
        content = content,
    )
}
