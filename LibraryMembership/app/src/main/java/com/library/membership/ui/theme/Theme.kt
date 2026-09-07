package com.library.membership.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LibraryColors = lightColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFF33691E),
)

@Composable
fun LibraryMembershipTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LibraryColors, content = content)
}
