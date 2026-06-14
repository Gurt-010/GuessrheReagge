package com.soundstation.guessrhereagge.ui.components.visualizer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import kotlin.math.sin

private const val BAR_COUNT = 9

/**
 * Reggae dub-inspired audio waveform visualizer with tri-color gradient bars.
 */
@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    barCount: Int = BAR_COUNT,
) {
    val density = LocalDensity.current
    val minBarHeightPx = with(density) { 10.dp.toPx() }
    val maxBarHeightPx = with(density) { 120.dp.toPx() }
    val barWidthPx = with(density) { 10.dp.toPx() }
    val barGapPx = with(density) { 8.dp.toPx() }

    val barSeeds = remember(barCount) { List(barCount) { index -> index * 1.37f + 0.5f } }
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 420 else 2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavePhase",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        val totalWidth = barCount * barWidthPx + (barCount - 1) * barGapPx
        var startX = (size.width - totalWidth) / 2f
        val baselineY = size.height * 0.92f

        barSeeds.forEachIndexed { index, seed ->
            val wave = sin(phase + seed * 2.1f).toFloat()
            val wave2 = sin(phase * 1.6f + seed * 3.3f).toFloat()
            val normalized = ((wave + wave2) * 0.5f + 1f) * 0.5f
            val animatedHeight = if (isActive) {
                minBarHeightPx + (maxBarHeightPx - minBarHeightPx) * normalized
            } else {
                minBarHeightPx + (maxBarHeightPx - minBarHeightPx) * 0.08f
            }

            val heightRatio = ((animatedHeight - minBarHeightPx) / (maxBarHeightPx - minBarHeightPx))
                .coerceIn(0f, 1f)
            val topColor = lerp(ReggaeColors.LionGreen, ReggaeColors.SunshineGold, heightRatio * 0.65f)
            val bottomColor = lerp(ReggaeColors.SunshineGold, ReggaeColors.FireRed, heightRatio)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(bottomColor, topColor, ReggaeColors.LionGreen),
                    startY = baselineY - animatedHeight,
                    endY = baselineY,
                ),
                topLeft = Offset(startX, baselineY - animatedHeight),
                size = Size(barWidthPx, animatedHeight),
                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f),
            )
            startX += barWidthPx + barGapPx
        }
    }
}
