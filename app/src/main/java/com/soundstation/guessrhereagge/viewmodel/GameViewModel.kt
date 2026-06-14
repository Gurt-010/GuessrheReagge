package com.soundstation.guessrhereagge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soundstation.guessrhereagge.audio.ExoPlayerAudioEngine
import com.soundstation.guessrhereagge.audio.PlaybackState
import com.soundstation.guessrhereagge.audio.YouTubeMusicAudioEngine
import com.soundstation.guessrhereagge.audio.youtube.YouTubeTrackStreamResolver
import com.soundstation.guessrhereagge.cast.AudioCastRouter
import com.soundstation.guessrhereagge.data.localization.toMessage
import com.soundstation.guessrhereagge.data.model.GameError
import com.soundstation.guessrhereagge.data.model.Track
import com.soundstation.guessrhereagge.data.preferences.GamePreferences
import com.soundstation.guessrhereagge.data.repository.YouTubeMusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GamePhase {
    LOADING,
    PLAYING,
    REVEALED,
    FINISHED,
}

data class GameUiState(
    val phase: GamePhase = GamePhase.LOADING,
    val currentRound: Int = 0,
    val totalRounds: Int = 10,
    val currentTrack: Track? = null,
    val isMetadataRevealed: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val errorMessage: String? = null,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GamePreferences(application)
    private val repository = YouTubeMusicRepository()
    private val streamResolver = YouTubeTrackStreamResolver()
    private val audioEngine: YouTubeMusicAudioEngine = ExoPlayerAudioEngine(application)
    private val audioCastRouter = AudioCastRouter(audioEngine)

    private var lastResolvedStreamUrl: String = ""

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = audioEngine.playbackState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaybackState.IDLE)

    private var playlist: List<Track> = emptyList()

    init {
        viewModelScope.launch {
            audioEngine.playbackState.collect { state ->
                _uiState.value = _uiState.value.copy(playbackState = state)
                if (state == PlaybackState.ERROR) {
                    _uiState.value.currentTrack?.let { track ->
                        streamResolver.invalidate(track.id)
                    }
                    _uiState.value = _uiState.value.copy(
                        errorMessage = GameError.STREAM_RESOLUTION_FAILED.toMessage(getApplication()),
                    )
                }
            }
        }
        startGame()
    }

    private fun startGame() {
        viewModelScope.launch {
            try {
                _uiState.value = GameUiState(phase = GamePhase.LOADING)
                val settings = preferences.settings.first()
                audioEngine.setVolume(settings.volume)
                audioCastRouter.rememberUserVolume(settings.volume)

                if (!settings.isYouTubeMusicConnected) {
                    _uiState.value = _uiState.value.copy(
                        phase = GamePhase.FINISHED,
                        errorMessage = GameError.YOUTUBE_NOT_CONNECTED.toMessage(getApplication()),
                    )
                    return@launch
                }

                val trackCount = if (settings.isEndlessRounds) 30 else settings.numberOfRounds
                playlist = repository.fetchPlaylistTracks(
                    genres = settings.selectedGenres,
                    count = trackCount,
                )

                if (playlist.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        phase = GamePhase.FINISHED,
                        errorMessage = GameError.NO_TRACKS_FOUND.toMessage(getApplication()),
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    totalRounds = if (settings.isEndlessRounds) {
                        com.soundstation.guessrhereagge.data.model.GameSettings.ENDLESS_ROUNDS
                    } else {
                        playlist.size
                    },
                    currentRound = 1,
                )
                loadRound(0, settings.isAudioPreRollEnabled)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = GamePhase.FINISHED,
                    errorMessage = GameError.STREAM_RESOLUTION_FAILED.toMessage(getApplication()),
                )
            }
        }
    }

    private suspend fun loadRound(index: Int, preRollNext: Boolean = false) {
        val track = playlist.getOrNull(index) ?: run {
            _uiState.value = _uiState.value.copy(phase = GamePhase.FINISHED)
            return
        }

        _uiState.value = _uiState.value.copy(
            phase = GamePhase.LOADING,
            currentTrack = track,
            isMetadataRevealed = false,
            currentRound = index + 1,
            errorMessage = null,
        )
        audioEngine.stop()

        try {
            val stream = streamResolver.resolve(track)
            if (preRollNext) {
                prefetchNextTrack(index + 1)
            }
            _uiState.value = _uiState.value.copy(phase = GamePhase.PLAYING, errorMessage = null)
            lastResolvedStreamUrl = stream.url
            audioEngine.play(stream.url, stream.requestHeaders)
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                phase = GamePhase.PLAYING,
                errorMessage = GameError.STREAM_RESOLUTION_FAILED.toMessage(getApplication()),
                playbackState = PlaybackState.ERROR,
            )
        }
    }

    private fun prefetchNextTrack(index: Int) {
        val nextTrack = playlist.getOrNull(index) ?: return
        viewModelScope.launch {
            runCatching { streamResolver.resolve(nextTrack) }
        }
    }

    fun retryPlayback() {
        val track = _uiState.value.currentTrack ?: return
        viewModelScope.launch {
            streamResolver.invalidate(track.id)
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val stream = streamResolver.resolve(track)
                audioEngine.play(stream.url, stream.requestHeaders)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = GameError.STREAM_RESOLUTION_FAILED.toMessage(getApplication()),
                    playbackState = PlaybackState.ERROR,
                )
            }
        }
    }

    fun togglePlayPause() {
        when (audioEngine.playbackState.value) {
            PlaybackState.PLAYING -> audioEngine.pause()
            PlaybackState.PAUSED -> audioEngine.resume()
            PlaybackState.STOPPED, PlaybackState.IDLE, PlaybackState.ERROR -> retryPlayback()
            else -> Unit
        }
    }

    fun revealMetadata() {
        _uiState.value = _uiState.value.copy(
            isMetadataRevealed = true,
            phase = GamePhase.REVEALED,
        )
        audioEngine.pause()
    }

    fun nextRound() {
        val currentIndex = _uiState.value.currentRound
        val isEndless = _uiState.value.totalRounds ==
            com.soundstation.guessrhereagge.data.model.GameSettings.ENDLESS_ROUNDS
        if (!isEndless && currentIndex >= playlist.size) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.FINISHED)
            audioEngine.stop()
            return
        }
        viewModelScope.launch {
            val prefs = preferences.settings.first()
            val nextIndex = if (isEndless && currentIndex >= playlist.size) 0 else currentIndex
            loadRound(nextIndex, prefs.isAudioPreRollEnabled)
        }
    }

    fun stopGame() {
        audioEngine.stop()
    }

    fun getAudioCastRouter(): AudioCastRouter = audioCastRouter

    fun getLastStreamUrl(): String = lastResolvedStreamUrl

    /** Pauses gameplay when Cast disconnects unexpectedly; phone resumes full UI. */
    fun onCastDisconnected() {
        if (audioEngine.playbackState.value == PlaybackState.PLAYING) {
            audioEngine.pause()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
