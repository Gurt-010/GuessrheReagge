package com.soundstation.guessrhereagge.data.repository

import com.soundstation.guessrhereagge.data.model.AuthError
import com.soundstation.guessrhereagge.data.model.MusicGenre
import com.soundstation.guessrhereagge.data.model.Track
import kotlinx.coroutines.delay

/**
 * Result of a YouTube Music OAuth2 authentication attempt.
 */
data class AuthResult(
    val success: Boolean,
    val email: String? = null,
    val isPremium: Boolean = false,
    val error: AuthError? = null,
)

/**
 * Mock wrapper around YouTube Music API operations.
 *
 * Replace method bodies with real YouTube Data API / Music API calls once
 * API credentials and OAuth2 client IDs are configured.
 */
class YouTubeMusicRepository {

    companion object {
        private const val AUTH_SIMULATION_DELAY_MS = 1_500L
        private const val FETCH_SIMULATION_DELAY_MS = 800L
    }

    /**
     * Simulated sign-in with username/email and password.
     * Replace with real OAuth2 / YouTube Music API authentication in production.
     */
    suspend fun authenticate(username: String, password: String): AuthResult {
        delay(AUTH_SIMULATION_DELAY_MS)

        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isBlank()) {
            return AuthResult(success = false, error = AuthError.USERNAME_REQUIRED)
        }
        if (trimmedPassword.isBlank()) {
            return AuthResult(success = false, error = AuthError.PASSWORD_REQUIRED)
        }
        if (trimmedPassword.length < 6) {
            return AuthResult(success = false, error = AuthError.INVALID_PASSWORD)
        }

        val accountEmail = if ("@" in trimmedUsername) {
            trimmedUsername
        } else {
            "$trimmedUsername@gmail.com"
        }

        return AuthResult(
            success = true,
            email = accountEmail,
            isPremium = true,
        )
    }

    suspend fun signOut() {
        delay(300)
    }

    /** Returns whether the stored session is still valid and Premium. */
    suspend fun verifyPremiumStatus(): Boolean {
        delay(200)
        return true
    }

    /**
     * Fetches tracks from a YouTube Music playlist filtered by genre.
     * Audio streams are resolved at playback time from YouTube by artist and title.
     */
    suspend fun fetchPlaylistTracks(
        genres: Set<String>,
        count: Int,
    ): List<Track> {
        delay(FETCH_SIMULATION_DELAY_MS)
        val normalizedGenres = genres.map { MusicGenre.normalizeStorageKey(it) }.toSet()
        return ReggaeTrackCatalog.tracks
            .filter { track -> normalizedGenres.isEmpty() || track.genre in normalizedGenres }
            .shuffled()
            .take(count)
    }
}
