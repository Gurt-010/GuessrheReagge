package com.soundstation.guessrhereagge.data.model

import androidx.annotation.StringRes
import com.soundstation.guessrhereagge.R

/**
 * User-configurable game preferences persisted across sessions.
 */
data class GameSettings(
    val numberOfRounds: Int = 10,
    val isEndlessRounds: Boolean = false,
    val selectedGenres: Set<String> = MusicGenre.defaultSelection(),
    val volume: Float = 0.8f,
    val isYouTubeMusicConnected: Boolean = false,
    val isPremiumVerified: Boolean = false,
    val connectedAccountEmail: String? = null,
    val languageCode: String = AppLanguage.DUTCH.code,
    val isGuessTimerEnabled: Boolean = false,
    val guessTimerSeconds: Int = 30,
    val isAudioPreRollEnabled: Boolean = true,
) {
    val effectiveRoundCount: Int
        get() = if (isEndlessRounds) ENDLESS_ROUNDS else numberOfRounds

    companion object {
        const val ENDLESS_ROUNDS = -1
        val ROUND_PRESETS = listOf(5, 10, 20)
        val GUESS_TIMER_OPTIONS = listOf(15, 30, 60)
    }
}

enum class MusicGenre(@StringRes val labelRes: Int) {
    POP(R.string.genre_pop),
    ROCK(R.string.genre_rock),
    HIP_HOP(R.string.genre_hip_hop),
    ELECTRONIC(R.string.genre_electronic),
    JAZZ(R.string.genre_jazz),
    REGGAE(R.string.genre_reggae),
    ;

    val storageKey: String get() = name

    companion object {
        fun fromStorageKey(key: String): MusicGenre? =
            entries.firstOrNull { it.name == key || it.name.equals(key, ignoreCase = true) }

        fun normalizeStorageKey(key: String): String {
            entries.firstOrNull { it.name == key }?.let { return it.name }
            // Legacy persisted display names (pre-localization)
            return when (key) {
                "Pop" -> POP.name
                "Rock" -> ROCK.name
                "Hip-Hop" -> HIP_HOP.name
                "Electronic" -> ELECTRONIC.name
                "Jazz" -> JAZZ.name
                "Reggae" -> REGGAE.name
                else -> key
            }
        }

        fun defaultSelection(): Set<String> = setOf(REGGAE.name)
    }
}

enum class AuthError {
    USERNAME_REQUIRED,
    PASSWORD_REQUIRED,
    INVALID_PASSWORD,
}

enum class GameError {
    YOUTUBE_NOT_CONNECTED,
    NO_TRACKS_FOUND,
    STREAM_RESOLUTION_FAILED,
}
