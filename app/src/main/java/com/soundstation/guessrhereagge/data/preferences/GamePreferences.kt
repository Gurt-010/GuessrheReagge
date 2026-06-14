package com.soundstation.guessrhereagge.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.soundstation.guessrhereagge.data.model.AppLanguage
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.data.model.MusicGenre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

class GamePreferences(private val context: Context) {

    val settings: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        prefs.toGameSettings()
    }

    suspend fun getSettingsOnce(): GameSettings = settings.first()

    suspend fun updateSettings(transform: (GameSettings) -> GameSettings) {
        context.dataStore.edit { prefs ->
            val updated = transform(prefs.toGameSettings())
            prefs.writeGameSettings(updated)
        }
    }

    private fun Preferences.toGameSettings(): GameSettings = GameSettings(
        numberOfRounds = this[KEY_ROUNDS] ?: DEFAULT_ROUNDS,
        isEndlessRounds = this[KEY_ENDLESS] ?: false,
        selectedGenres = this[KEY_GENRES]
            ?.map { MusicGenre.normalizeStorageKey(it) }
            ?.toSet()
            ?: MusicGenre.defaultSelection(),
        volume = this[KEY_VOLUME] ?: DEFAULT_VOLUME,
        isYouTubeMusicConnected = this[KEY_CONNECTED] ?: false,
        isPremiumVerified = this[KEY_PREMIUM] ?: false,
        connectedAccountEmail = this[KEY_EMAIL],
        languageCode = this[KEY_LANGUAGE] ?: AppLanguage.DUTCH.code,
        isGuessTimerEnabled = this[KEY_GUESS_TIMER_ENABLED] ?: false,
        guessTimerSeconds = this[KEY_GUESS_TIMER_SECONDS] ?: DEFAULT_GUESS_TIMER,
        isAudioPreRollEnabled = this[KEY_PRE_ROLL] ?: true,
    )

    private fun MutablePreferences.writeGameSettings(updated: GameSettings) {
        this[KEY_ROUNDS] = updated.numberOfRounds
        this[KEY_ENDLESS] = updated.isEndlessRounds
        this[KEY_GENRES] = updated.selectedGenres
        this[KEY_VOLUME] = updated.volume
        this[KEY_CONNECTED] = updated.isYouTubeMusicConnected
        this[KEY_PREMIUM] = updated.isPremiumVerified
        this[KEY_LANGUAGE] = updated.languageCode
        this[KEY_GUESS_TIMER_ENABLED] = updated.isGuessTimerEnabled
        this[KEY_GUESS_TIMER_SECONDS] = updated.guessTimerSeconds
        this[KEY_PRE_ROLL] = updated.isAudioPreRollEnabled
        if (updated.connectedAccountEmail != null) {
            this[KEY_EMAIL] = updated.connectedAccountEmail
        } else {
            remove(KEY_EMAIL)
        }
    }

    companion object {
        private const val DEFAULT_ROUNDS = 10
        private const val DEFAULT_VOLUME = 0.8f
        private const val DEFAULT_GUESS_TIMER = 30

        private val KEY_ROUNDS = intPreferencesKey("number_of_rounds")
        private val KEY_ENDLESS = booleanPreferencesKey("endless_rounds")
        private val KEY_GENRES = stringSetPreferencesKey("selected_genres")
        private val KEY_VOLUME = floatPreferencesKey("volume")
        private val KEY_CONNECTED = booleanPreferencesKey("yt_music_connected")
        private val KEY_PREMIUM = booleanPreferencesKey("yt_music_premium")
        private val KEY_EMAIL = stringPreferencesKey("yt_music_email")
        private val KEY_LANGUAGE = stringPreferencesKey("language_code")
        private val KEY_GUESS_TIMER_ENABLED = booleanPreferencesKey("guess_timer_enabled")
        private val KEY_GUESS_TIMER_SECONDS = intPreferencesKey("guess_timer_seconds")
        private val KEY_PRE_ROLL = booleanPreferencesKey("audio_pre_roll_enabled")
    }
}
