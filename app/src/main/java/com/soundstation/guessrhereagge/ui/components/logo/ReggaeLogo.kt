package com.soundstation.guessrhereagge.ui.components.logo

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors

/**
 * Pulsing app logo with heartbeat-synchronized neon aura.
 */
@Composable
fun ReggaeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    contentDescription: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reggaeLogo")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoPulse",
    )
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoGlow",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val glowRadius = this.size.minDimension * 0.55f
            val center = this.center
            scale(scale = pulse, pivot = center) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ReggaeColors.NeonPurple.copy(alpha = glowIntensity * 0.55f),
                            ReggaeColors.NeonCyan.copy(alpha = glowIntensity * 0.25f),
                            Color.Transparent,
                        ),
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ReggaeColors.NeonPink.copy(alpha = glowIntensity * 0.15f),
                            Color.Transparent,
                        ),
                        radius = glowRadius * 0.75f,
                    ),
                    radius = glowRadius * 0.75f,
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.ic_app_logo),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size * 0.72f)
                .scale(pulse),
        )
    }
}
