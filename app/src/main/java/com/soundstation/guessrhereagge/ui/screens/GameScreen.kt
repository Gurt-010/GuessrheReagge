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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.LocalGameCastCoordinator
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.audio.PlaybackState
import com.soundstation.guessrhereagge.cast.CastConnectionState
import com.soundstation.guessrhereagge.cast.GameCastActions
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.ui.components.ReggaeCard
import com.soundstation.guessrhereagge.ui.components.ReggaeProgressBar
import com.soundstation.guessrhereagge.ui.components.ReggaeTopBar
import com.soundstation.guessrhereagge.ui.components.background.ReggaeBackground
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButton
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButtonVariant
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeIconButton
import com.soundstation.guessrhereagge.ui.components.cast.CastMediaRouteButton
import com.soundstation.guessrhereagge.ui.components.visualizer.AudioWaveformVisualizer
import com.soundstation.guessrhereagge.ui.components.vinyl.SpinningVinyl
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography
import com.soundstation.guessrhereagge.viewmodel.GamePhase
import com.soundstation.guessrhereagge.viewmodel.GameUiState
import com.soundstation.guessrhereagge.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val castCoordinator = LocalGameCastCoordinator.current
    val castState by castCoordinator?.connectionState?.collectAsState()
        ?: remember { androidx.compose.runtime.mutableStateOf(CastConnectionState.DISCONNECTED) }

    val gameActions = remember(viewModel) {
        object : GameCastActions {
            override fun togglePlayPause() = viewModel.togglePlayPause()
            override fun revealMetadata() = viewModel.revealMetadata()
            override fun nextRound() = viewModel.nextRound()
            override fun retryPlayback() = viewModel.retryPlayback()
        }
    }

    DisposableEffect(viewModel, castCoordinator) {
        castCoordinator?.bindGameViewModel(viewModel)
        castCoordinator?.setOnUnexpectedDisconnectListener {
            viewModel.onCastDisconnected()
        }
        onDispose {
            castCoordinator?.unbindGameViewModel()
            castCoordinator?.setOnUnexpectedDisconnectListener(null)
        }
    }

    LaunchedEffect(viewModel.getLastStreamUrl()) {
        val url = viewModel.getLastStreamUrl()
        if (url.isNotBlank()) {
            castCoordinator?.updateStreamUrl(url)
        }
    }

    val isCastMode = castState == CastConnectionState.CONNECTED ||
        castState == CastConnectionState.CONNECTING

    if (isCastMode) {
        CastControllerScreen(
            uiState = uiState,
            actions = gameActions,
            onNavigateBack = onNavigateBack,
        )
        return
    }

    ReggaeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            ReggaeTopBar(
                title = when {
                    uiState.phase == GamePhase.FINISHED || uiState.totalRounds == 0 ->
                        stringResource(R.string.game_title_short)
                    uiState.totalRounds == GameSettings.ENDLESS_ROUNDS ->
                        stringResource(R.string.game_round_endless, uiState.currentRound)
                    else ->
                        stringResource(
                            R.string.game_round_progress,
                            uiState.currentRound,
                            uiState.totalRounds,
                        )
                },
                onNavigateBack = onNavigateBack,
                actions = { CastMediaRouteButton() },
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
            ) {
                when (uiState.phase) {
                    GamePhase.LOADING -> LoadingContent()
                    GamePhase.PLAYING, GamePhase.REVEALED -> GameplayContent(
                        uiState = uiState,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onReveal = viewModel::revealMetadata,
                        onNextRound = viewModel::nextRound,
                        onRetry = viewModel::retryPlayback,
                    )
                    GamePhase.FINISHED -> FinishedContent(
                        errorMessage = uiState.errorMessage,
                        onNavigateBack = onNavigateBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = ReggaeColors.SunshineGold,
                trackColor = ReggaeColors.LionGreen.copy(alpha = 0.25f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.game_loading),
                style = ReggaeTypography.bodyLarge,
                color = ReggaeColors.TextGold,
            )
        }
    }
}

@Composable
private fun GameplayContent(
    uiState: GameUiState,
    onTogglePlayPause: () -> Unit,
    onReveal: () -> Unit,
    onNextRound: () -> Unit,
    onRetry: () -> Unit,
) {
    val isPlaying = uiState.playbackState == PlaybackState.PLAYING
    val isPaused = uiState.playbackState == PlaybackState.PAUSED
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
        ReggaeProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            if (uiState.isMetadataRevealed) {
                RevealedTrackCard(track = uiState.currentTrack)
            } else {
                HiddenTrackCard(
                    playbackState = uiState.playbackState,
                    isPlaying = isPlaying,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isPaused && !uiState.isMetadataRevealed) {
                RowWithCastButton()
            }

            if (!uiState.isMetadataRevealed) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        style = ReggaeTypography.bodyMedium,
                        color = ReggaeColors.FireRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ReggaeButton(
                        text = stringResource(R.string.game_retry),
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ReggaeButtonVariant.Outline,
                    )
                }

                ReggaeIconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.game_play_pause),
                        modifier = Modifier.size(36.dp),
                        tint = ReggaeColors.SunshineGold,
                    )
                }

                ReggaeButton(
                    text = stringResource(R.string.game_reveal),
                    onClick = onReveal,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ReggaeButtonVariant.Primary,
                )
            } else {
                ReggaeButton(
                    text = if (!isEndless && uiState.currentRound >= uiState.totalRounds) {
                        stringResource(R.string.game_finish)
                    } else {
                        stringResource(R.string.game_next_round)
                    },
                    onClick = onNextRound,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ReggaeButtonVariant.Secondary,
                )
            }
        }
    }
}

@Composable
private fun RowWithCastButton() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        CastMediaRouteButton()
    }
}

@Composable
private fun HiddenTrackCard(
    playbackState: PlaybackState,
    isPlaying: Boolean,
) {
    ReggaeCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(contentAlignment = Alignment.Center) {
                SpinningVinyl(
                    isPlaying = isPlaying,
                    modifier = Modifier.size(160.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AudioWaveformVisualizer(
                isActive = isPlaying,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.game_guess_prompt),
                style = ReggaeTypography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = ReggaeColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (playbackState) {
                    PlaybackState.LOADING -> stringResource(R.string.game_status_loading)
                    PlaybackState.PLAYING -> stringResource(R.string.game_status_playing)
                    PlaybackState.PAUSED -> stringResource(R.string.game_status_paused)
                    PlaybackState.ERROR -> stringResource(R.string.game_status_error)
                    else -> stringResource(R.string.game_status_ready)
                },
                style = ReggaeTypography.titleMedium,
                color = ReggaeColors.TextGold,
            )
        }
    }
}

@Composable
private fun RevealedTrackCard(track: com.soundstation.guessrhereagge.data.model.Track?) {
    ReggaeCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = track?.title ?: "—",
                style = ReggaeTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = ReggaeColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = track?.artist ?: "—",
                style = ReggaeTypography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = track?.releaseYear?.toString() ?: "—",
                style = ReggaeTypography.displayLarge.copy(
                    color = ReggaeColors.LionGreen,
                    fontWeight = FontWeight.Black,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.game_release_year_label),
                style = ReggaeTypography.bodyMedium,
                color = ReggaeColors.TextGold.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun FinishedContent(
    errorMessage: String?,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = errorMessage ?: stringResource(R.string.game_finished),
            style = ReggaeTypography.headlineMedium,
            textAlign = TextAlign.Center,
            color = ReggaeColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        ReggaeButton(
            text = stringResource(R.string.game_back_to_menu),
            onClick = onNavigateBack,
            variant = ReggaeButtonVariant.Outline,
        )
    }
}
