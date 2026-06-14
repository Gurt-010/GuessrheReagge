package com.soundstation.guessrhereagge.ui.components.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import kotlin.math.sin
import kotlin.random.Random

/**
 * Reusable immersive Reggae background with animated gradient, vinyl grooves, and noise overlay.
 */
@Composable
fun ReggaeBackground(
    modifier: Modifier = Modifier,
    animateGradient: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reggaeBg")
    val gradientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gradientPhase",
    )

    val noisePoints = remember { generateNoisePoints(seed = 42, count = 280) }
    val density = LocalDensity.current
    val grooveSpacingPx = with(density) { 14.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAnimatedGradient(size.width, size.height, gradientPhase, animateGradient)
            drawVinylGrooves(grooveSpacingPx, gradientPhase)
            drawNoiseOverlay(noisePoints)
        }
        content()
    }
}

private fun DrawScope.drawAnimatedGradient(
    width: Float,
    height: Float,
    phase: Float,
    animate: Boolean,
) {
    val offset = if (animate) phase else 0.35f
    val centerX = width * (0.35f + offset * 0.3f)
    val centerY = height * (0.45f - offset * 0.15f)

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                ReggaeColors.BackgroundOlive,
                ReggaeColors.BackgroundCharcoal,
                ReggaeColors.BackgroundCharcoal,
                ReggaeColors.FireRed.copy(alpha = 0.22f),
            ),
            center = Offset(centerX, centerY),
            radius = width.coerceAtLeast(height) * 0.95f,
        ),
    )

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                ReggaeColors.LionGreen.copy(alpha = 0.08f),
                Color.Transparent,
                ReggaeColors.FireRed.copy(alpha = 0.12f),
            ),
            start = Offset(width * offset, 0f),
            end = Offset(width * (1f - offset), height),
        ),
    )
}

private fun DrawScope.drawVinylGrooves(spacingPx: Float, phase: Float) {
    val grooveColor = ReggaeColors.LionGreen.copy(alpha = 0.035f)
    var radius = spacingPx * 2f
    val maxRadius = size.minDimension * 0.85f
    while (radius < maxRadius) {
        val alphaMod = 0.5f + 0.5f * sin(radius * 0.02f + phase * 6.28f).toFloat()
        drawCircle(
            color = grooveColor.copy(alpha = grooveColor.alpha * alphaMod),
            radius = radius,
            center = Offset(size.width * 0.5f, size.height * 0.55f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f),
        )
        radius += spacingPx
    }
}

private fun DrawScope.drawNoiseOverlay(points: List<Offset>) {
    points.forEach { point ->
        val x = point.x * size.width
        val y = point.y * size.height
        drawCircle(
            color = ReggaeColors.OverlayNoise,
            radius = 1.1f,
            center = Offset(x, y),
        )
    }
}

private fun generateNoisePoints(seed: Int, count: Int): List<Offset> {
    val random = Random(seed)
    return List(count) {
        Offset(random.nextFloat(), random.nextFloat())
    }
}
