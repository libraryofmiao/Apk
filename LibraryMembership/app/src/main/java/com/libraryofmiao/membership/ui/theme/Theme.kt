package com.libraryofmiao.membership.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentBlue = Color(0xFF2563EB)
val AccentBlueLight = Color(0xFF3B82F6)
val LoginBg = Color(0xFF020617)
val LoginCardBg = Color(0xFF0F172A)
val LoginBorder = Color(0xFF334155)
val TextLight = Color(0xFFE2E8F0)
val TextMuted = Color(0xFF94A3B8)
val ErrorRed = Color(0xFFF87171)
val SurfaceLight = Color(0xFFF5F5F5)
val StatusActiveBg = Color(0xFFD1E7DD)
val StatusActiveText = Color(0xFF0F5132)
val StatusInactiveBg = Color(0xFFF8D7DA)
val StatusInactiveText = Color(0xFF842029)

private val LightColors = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentBlueLight,
    background = SurfaceLight,
    surface = Color.White,
    error = Color(0xFFDC3545)
)

@Composable
fun LibraryMembershipTheme(content: @Composable () -> Unit) {
    // The app intentionally always uses the light scheme for data screens (matches
    // dashboard.html's Bootstrap look); the Login screen paints its own dark background
    // directly, mirroring login.html, regardless of system theme.
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
