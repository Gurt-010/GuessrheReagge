package com.soundstation.guessrhereagge.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography

val ReggaeButtonShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 8.dp,
    bottomStart = 8.dp,
    bottomEnd = 24.dp,
)

enum class ReggaeButtonVariant {
    Primary,
    Secondary,
    Outline,
}

@Composable
fun ReggaeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ReggaeButtonVariant = ReggaeButtonVariant.Primary,
    enabled: Boolean = true,
    minHeight: Dp = 56.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "reggaeButtonScale",
    )
    val backgroundAlpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.35f
            isPressed -> 0.72f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 120),
        label = "reggaeButtonAlpha",
    )

    val fillBrush = when (variant) {
        ReggaeButtonVariant.Primary -> Brush.linearGradient(
            colors = listOf(
                ReggaeColors.LionGreen.copy(alpha = backgroundAlpha),
                ReggaeColors.LionGreen.copy(alpha = backgroundAlpha * 0.75f),
            ),
        )
        ReggaeButtonVariant.Secondary -> Brush.linearGradient(
            colors = listOf(
                ReggaeColors.SunshineGold.copy(alpha = backgroundAlpha * 0.25f),
                ReggaeColors.SurfaceElevated.copy(alpha = backgroundAlpha),
            ),
        )
        ReggaeButtonVariant.Outline -> Brush.linearGradient(
            colors = listOf(
                ReggaeColors.SurfaceDark.copy(alpha = backgroundAlpha * 0.6f),
                ReggaeColors.BackgroundOlive.copy(alpha = backgroundAlpha * 0.4f),
            ),
        )
    }

    val textColor = when (variant) {
        ReggaeButtonVariant.Secondary -> ReggaeColors.SunshineGold
        ReggaeButtonVariant.Outline -> ReggaeColors.TextPrimary
        ReggaeButtonVariant.Primary -> ReggaeColors.TextPrimary
    }

    Box(
        modifier = modifier
            .scale(scale)
            .semantics { role = Role.Button }
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = ReggaeButtonShape,
                ambientColor = ReggaeColors.LionGreen.copy(alpha = 0.35f),
                spotColor = ReggaeColors.SunshineGold.copy(alpha = 0.25f),
            )
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val halfStroke = strokeWidth / 2f
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ReggaeColors.LionGreen,
                            ReggaeColors.SunshineGold,
                            ReggaeColors.FireRed,
                            ReggaeColors.LionGreen,
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                    ),
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = strokeWidth),
                )
            }
            .clip(ReggaeButtonShape)
            .background(fillBrush)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = minHeight)
            .padding(PaddingValues(horizontal = 24.dp, vertical = 14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ReggaeTypography.labelLarge.copy(color = textColor),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ReggaeIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = tween(120),
        label = "reggaeIconScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, ReggaeButtonShape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        listOf(
                            ReggaeColors.LionGreen,
                            ReggaeColors.SunshineGold,
                            ReggaeColors.FireRed,
                            ReggaeColors.LionGreen,
                        ),
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(1.5.dp.toPx()),
                )
            }
            .clip(ReggaeButtonShape)
            .background(
                if (isPressed) ReggaeColors.SurfaceElevated.copy(alpha = 0.85f)
                else ReggaeColors.SurfaceDark.copy(alpha = 0.9f),
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
