package com.soundstation.guessrhereagge.audio

/**
 * Playback state exposed to the UI layer.
 */
enum class PlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR,
}

/**
 * Contract for internal YouTube Music audio playback.
 *
 * Production implementations would resolve YouTube Music stream URLs via
 * the YouTube Android Player API or an authenticated WebView session.
 * This interface keeps ViewModels decoupled from the concrete player.
 */
interface YouTubeMusicAudioEngine {
    val playbackState: kotlinx.coroutines.flow.StateFlow<PlaybackState>
    val currentPositionMs: kotlinx.coroutines.flow.StateFlow<Long>
    val durationMs: kotlinx.coroutines.flow.StateFlow<Long>

    /** Load and begin playback of the given stream URL. */
    suspend fun play(streamUrl: String, requestHeaders: Map<String, String> = emptyMap())

    fun pause()

    fun resume()

    /** Stop playback and release buffered media. */
    fun stop()

    /** Release all player resources — call from ViewModel.onCleared(). */
    fun release()

    fun setVolume(volume: Float)
}
