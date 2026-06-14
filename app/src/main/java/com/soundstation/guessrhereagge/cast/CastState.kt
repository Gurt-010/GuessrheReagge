package com.soundstation.guessrhereagge.cast

/**
 * Connection state exposed to the UI layer for dual-screen mode switching.
 */
enum class CastConnectionState {
    /** Phone renders the full game; no external display or Cast session. */
    DISCONNECTED,

    /** Cast session or secondary display is active; phone becomes controller-only. */
    CONNECTED,

    /** Transient state while establishing a Cast route or Presentation. */
    CONNECTING,

    /** Recoverable failure (network drop, receiver unavailable). */
    ERROR,
}

/**
 * Actions invoked from the phone controller and forwarded to the game ViewModel.
 */
interface GameCastActions {
    fun togglePlayPause()
    fun revealMetadata()
    fun nextRound()
    fun retryPlayback()
}
