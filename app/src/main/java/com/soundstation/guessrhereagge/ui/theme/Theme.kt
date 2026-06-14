package com.soundstation.guessrhereagge.ui.theme

import androidx.compose.runtime.Composable

/** @deprecated Use [ReggaeTheme] — kept for backward compatibility. */
@Composable
fun GuessrTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    ReggaeTheme(content = content)
}
