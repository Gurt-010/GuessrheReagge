package com.soundstation.guessrhereagge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors

val ReggaeCardShape = RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 6.dp, bottomEnd = 20.dp)

@Composable
fun ReggaeCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ReggaeCardShape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(
                            ReggaeColors.LionGreen.copy(alpha = 0.55f),
                            ReggaeColors.SunshineGold.copy(alpha = 0.35f),
                            ReggaeColors.FireRed.copy(alpha = 0.45f),
                        ),
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx()),
                )
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ReggaeColors.SurfaceElevated.copy(alpha = 0.95f),
                        ReggaeColors.SurfaceDark.copy(alpha = 0.88f),
                    ),
                ),
            )
            .padding(24.dp),
    ) {
        content()
    }
}

@Composable
fun ReggaeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(ReggaeColors.BackgroundOlive.copy(alpha = 0.8f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ReggaeColors.LionGreen,
                            ReggaeColors.SunshineGold,
                            ReggaeColors.FireRed,
                        ),
                    ),
                ),
        )
    }
}
