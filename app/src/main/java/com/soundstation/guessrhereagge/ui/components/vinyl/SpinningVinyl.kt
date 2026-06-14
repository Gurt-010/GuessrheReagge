package com.soundstation.guessrhereagge.ui.components.vinyl

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import kotlinx.coroutines.isActive

/**
 * Spinning vinyl record — rotates while playing, decelerates smoothly when paused.
 */
@Composable
fun SpinningVinyl(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val rotationAnimatable = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                rotationAnimatable.animateTo(
                    targetValue = rotationAnimatable.value + 360f,
                    animationSpec = tween(durationMillis = 3_200, easing = LinearEasing),
                )
                rotationAnimatable.snapTo(rotationAnimatable.value % 360f)
            }
        }
    }

    val dimOverlay by animateFloatAsState(
        targetValue = if (isPlaying) 0f else 0.15f,
        animationSpec = tween(400),
        label = "vinylDim",
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(rotationAnimatable.value % 360f, center) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ReggaeColors.BackgroundCharcoal,
                        ReggaeColors.SurfaceDark,
                        ReggaeColors.BackgroundCharcoal,
                    ),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            var grooveRadius = radius * 0.35f
            while (grooveRadius < radius * 0.95f) {
                drawCircle(
                    color = ReggaeColors.LionGreen.copy(alpha = 0.12f),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = 1f),
                )
                grooveRadius += radius * 0.06f
            }

            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ReggaeColors.LionGreen,
                        ReggaeColors.SunshineGold,
                        ReggaeColors.FireRed,
                        ReggaeColors.LionGreen,
                    ),
                ),
                radius = radius * 0.22f,
                center = center,
                style = Stroke(width = radius * 0.04f),
            )

            drawCircle(color = ReggaeColors.FireRed, radius = radius * 0.08f, center = center)
            drawCircle(color = ReggaeColors.SunshineGold, radius = radius * 0.04f, center = center)
        }

        if (dimOverlay > 0f) {
            drawCircle(
                color = Color.Black.copy(alpha = dimOverlay),
                radius = radius,
                center = center,
            )
        }
    }
}
