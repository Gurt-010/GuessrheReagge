package com.soundstation.guessrhereagge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.audio.PlaybackState
import com.soundstation.guessrhereagge.cast.GameCastActions
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.ui.components.ReggaeTopBar
import com.soundstation.guessrhereagge.ui.components.background.ReggaeBackground
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButton
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButtonVariant
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeIconButton
import com.soundstation.guessrhereagge.ui.components.cast.CastMediaRouteButton
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography
import com.soundstation.guessrhereagge.viewmodel.GamePhase
import com.soundstation.guessrhereagge.viewmodel.GameUiState

/**
 * Phone-only controller UI shown while the TV renders gameplay via [GamePresentation].
 */
@Composable
fun CastControllerScreen(
    uiState: GameUiState,
    actions: GameCastActions,
    onNavigateBack: () -> Unit,
) {
    val isPlaying = uiState.playbackState == PlaybackState.PLAYING
    val isEndless = uiState.totalRounds == GameSettings.ENDLESS_ROUNDS

    ReggaeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            ReggaeTopBar(
                title = stringResource(R.string.cast_controller_title),
                onNavigateBack = onNavigateBack,
                actions = { CastMediaRouteButton() },
            )

            Text(
                text = stringResource(R.string.cast_controller_subtitle),
                style = ReggaeTypography.bodyMedium,
                color = ReggaeColors.TextGold.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )

            TouchPadArea(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.phase == GamePhase.PLAYING && !uiState.isMetadataRevealed) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ReggaeIconButton(onClick = actions::togglePlayPause) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.game_play_pause),
                                modifier = Modifier.size(40.dp),
                                tint = ReggaeColors.SunshineGold,
                            )
                        }
                    }

                    ReggaeButton(
                        text = stringResource(R.string.game_reveal),
                        onClick = actions::revealMetadata,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ReggaeButtonVariant.Primary,
                    )

                    if (uiState.errorMessage != null) {
                        ReggaeButton(
                            text = stringResource(R.string.game_retry),
                            onClick = actions::retryPlayback,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ReggaeButtonVariant.Outline,
                        )
                    }
                } else if (uiState.isMetadataRevealed) {
                    ReggaeButton(
                        text = if (!isEndless && uiState.currentRound >= uiState.totalRounds) {
                            stringResource(R.string.game_finish)
                        } else {
                            stringResource(R.string.game_next_round)
                        },
                        onClick = actions::nextRound,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ReggaeButtonVariant.Secondary,
                    )
                } else if (uiState.phase == GamePhase.FINISHED) {
                    ReggaeButton(
                        text = stringResource(R.string.game_back_to_menu),
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ReggaeButtonVariant.Outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun TouchPadArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(ReggaeColors.LionGreen.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectDragGestures { _, _ ->
                    // Reserved for future navigation gestures; keeps touch pipeline warm for low latency.
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = ReggaeColors.SunshineGold.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.cast_touchpad_hint),
                style = ReggaeTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ReggaeColors.TextGold.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
