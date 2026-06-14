package com.soundstation.guessrhereagge.cast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Letterboxes child content to 16:9 within any TV panel size (1080p, 4K, ultrawide).
 */
@Composable
fun AspectRatio16x9Container(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val targetAspect = GamePresentation.ASPECT_RATIO_16_9
        val boxModifier = if (maxWidth / maxHeight > targetAspect) {
            Modifier.fillMaxHeight().aspectRatio(targetAspect)
        } else {
            Modifier.fillMaxWidth().aspectRatio(targetAspect)
        }

        Box(modifier = boxModifier) {
            content()
        }
    }
}
