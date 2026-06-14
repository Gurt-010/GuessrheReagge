package com.soundstation.guessrhereagge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.audio.PlaybackState
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.ui.components.ReggaeCard
import com.soundstation.guessrhereagge.ui.components.ReggaeProgressBar
import com.soundstation.guessrhereagge.ui.components.background.ReggaeBackground
import com.soundstation.guessrhereagge.ui.components.visualizer.AudioWaveformVisualizer
import com.soundstation.guessrhereagge.ui.components.vinyl.SpinningVinyl
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography
import com.soundstation.guessrhereagge.viewmodel.GamePhase
import com.soundstation.guessrhereagge.viewmodel.GameUiState

/**
 * Gameplay visuals rendered on the TV via [com.soundstation.guessrhereagge.cast.GamePresentation].
 * No interactive controls — input is handled on the phone controller screen.
 */
@Composable
fun GameTvContent(uiState: GameUiState) {
    ReggaeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
        ) {
            when (uiState.phase) {
                GamePhase.LOADING -> TvLoadingContent()
                GamePhase.PLAYING, GamePhase.REVEALED -> TvGameplayContent(uiState)
                GamePhase.FINISHED -> TvFinishedContent(uiState.errorMessage)
            }
        }
    }
}

@Composable
private fun TvLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = ReggaeColors.SunshineGold,
                trackColor = ReggaeColors.LionGreen.copy(alpha = 0.25f),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.game_loading),
                style = ReggaeTypography.headlineSmall,
                color = ReggaeColors.TextGold,
            )
        }
    }
}

@Composable
private fun TvGameplayContent(uiState: GameUiState) {
    val isPlaying = uiState.playbackState == PlaybackState.PLAYING
    val isEndless = uiState.totalRounds == GameSettings.ENDLESS_ROUNDS
    val progress = when {
        isEndless -> ((uiState.currentRound % 10).coerceAtLeast(1)).toFloat() / 10f
        uiState.totalRounds > 0 -> uiState.currentRound.toFloat() / uiState.totalRounds
        else -> 0f
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = when {
                uiState.totalRounds == GameSettings.ENDLESS_ROUNDS ->
                    stringResource(R.string.game_round_endless, uiState.currentRound)
                uiState.totalRounds > 0 ->
                    stringResource(
                        R.string.game_round_progress,
                        uiState.currentRound,
                        uiState.totalRounds,
                    )
                else -> stringResource(R.string.game_title_short)
            },
            style = ReggaeTypography.titleLarge.copy(
                color = ReggaeColors.TextGold,
                fontWeight = FontWeight.Bold,
            ),
        )

        ReggaeProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            if (uiState.isMetadataRevealed) {
                TvRevealedTrackCard(track = uiState.currentTrack)
            } else {
                TvHiddenTrackCard(
                    playbackState = uiState.playbackState,
                    isPlaying = isPlaying,
                )
            }
        }

        if (uiState.errorMessage != null && !uiState.isMetadataRevealed) {
            Text(
                text = uiState.errorMessage,
                style = ReggaeTypography.bodyLarge,
                color = ReggaeColors.FireRed,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TvHiddenTrackCard(
    playbackState: PlaybackState,
    isPlaying: Boolean,
) {
    ReggaeCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SpinningVinyl(
                isPlaying = isPlaying,
                modifier = Modifier.size(220.dp),
            )
            Spacer(modifier = Modifier.height(28.dp))
            AudioWaveformVisualizer(
                isActive = isPlaying,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.game_guess_prompt),
                style = ReggaeTypography.displaySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = ReggaeColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when (playbackState) {
                    PlaybackState.LOADING -> stringResource(R.string.game_status_loading)
                    PlaybackState.PLAYING -> stringResource(R.string.game_status_playing)
                    PlaybackState.PAUSED -> stringResource(R.string.game_status_paused)
                    PlaybackState.ERROR -> stringResource(R.string.game_status_error)
                    else -> stringResource(R.string.game_status_ready)
                },
                style = ReggaeTypography.headlineSmall,
                color = ReggaeColors.TextGold,
            )
        }
    }
}

@Composable
private fun TvRevealedTrackCard(track: com.soundstation.guessrhereagge.data.model.Track?) {
    ReggaeCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = track?.title ?: "—",
                style = ReggaeTypography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = ReggaeColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = track?.artist ?: "—",
                style = ReggaeTypography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = track?.releaseYear?.toString() ?: "—",
                style = ReggaeTypography.displayLarge.copy(
                    color = ReggaeColors.LionGreen,
                    fontWeight = FontWeight.Black,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.game_release_year_label),
                style = ReggaeTypography.titleMedium,
                color = ReggaeColors.TextGold.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun TvFinishedContent(errorMessage: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = errorMessage ?: stringResource(R.string.game_finished),
            style = ReggaeTypography.headlineMedium,
            textAlign = TextAlign.Center,
            color = ReggaeColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 48.dp),
        )
    }
}
