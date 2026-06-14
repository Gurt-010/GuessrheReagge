package com.soundstation.guessrhereagge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soundstation.guessrhereagge.data.localization.LocaleManager
import com.soundstation.guessrhereagge.data.localization.toMessage
import com.soundstation.guessrhereagge.data.model.AppLanguage
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.data.model.MusicGenre
import com.soundstation.guessrhereagge.data.preferences.GamePreferences
import com.soundstation.guessrhereagge.data.repository.YouTubeMusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: GameSettings = GameSettings(),
    val showLoginDialog: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val isAboutExpanded: Boolean = false,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GamePreferences(application)
    private val repository = YouTubeMusicRepository()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val settings: StateFlow<GameSettings> = preferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameSettings())

    init {
        viewModelScope.launch {
            preferences.settings.collect { gameSettings ->
                _uiState.value = _uiState.value.copy(settings = gameSettings)
            }
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            preferences.updateSettings { it.copy(volume = volume) }
        }
    }

    fun setNumberOfRounds(rounds: Int) {
        viewModelScope.launch {
            preferences.updateSettings {
                it.copy(
                    numberOfRounds = rounds.coerceIn(3, 30),
                    isEndlessRounds = false,
                )
            }
        }
    }

    fun setRoundPreset(rounds: Int) {
        viewModelScope.launch {
            preferences.updateSettings {
                it.copy(numberOfRounds = rounds, isEndlessRounds = false)
            }
        }
    }

    fun setEndlessRounds(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateSettings { it.copy(isEndlessRounds = enabled) }
        }
    }

    fun setGuessTimerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateSettings { it.copy(isGuessTimerEnabled = enabled) }
        }
    }

    fun setGuessTimerSeconds(seconds: Int) {
        viewModelScope.launch {
            preferences.updateSettings {
                it.copy(guessTimerSeconds = seconds.coerceIn(15, 60))
            }
        }
    }

    fun setAudioPreRollEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateSettings { it.copy(isAudioPreRollEnabled = enabled) }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferences.updateSettings { it.copy(languageCode = language.code) }
            LocaleManager.applyLanguage(language)
        }
    }

    fun toggleAboutExpanded() {
        _uiState.value = _uiState.value.copy(isAboutExpanded = !_uiState.value.isAboutExpanded)
    }

    fun toggleGenre(genre: MusicGenre) {
        viewModelScope.launch {
            preferences.updateSettings { current ->
                val updated = current.selectedGenres.toMutableSet()
                val key = genre.storageKey
                if (key in updated) {
                    updated.remove(key)
                } else {
                    updated.add(key)
                }
                current.copy(selectedGenres = updated)
            }
        }
    }

    fun openLoginDialog() {
        _uiState.value = _uiState.value.copy(showLoginDialog = true, authError = null)
    }

    fun dismissLoginDialog() {
        if (_uiState.value.isAuthenticating) return
        _uiState.value = _uiState.value.copy(showLoginDialog = false, authError = null)
    }

    fun connectYouTubeMusic(username: String, password: String) {
        if (_uiState.value.isAuthenticating) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthenticating = true, authError = null)
            val result = repository.authenticate(username, password)
            if (result.success && result.isPremium) {
                preferences.updateSettings {
                    it.copy(
                        isYouTubeMusicConnected = true,
                        isPremiumVerified = true,
                        connectedAccountEmail = result.email,
                    )
                }
                _uiState.value = _uiState.value.copy(
                    showLoginDialog = false,
                    isAuthenticating = false,
                    authError = null,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    authError = result.error?.toMessage(getApplication()),
                )
            }
        }
    }

    fun disconnectYouTubeMusic() {
        viewModelScope.launch {
            repository.signOut()
            preferences.updateSettings {
                it.copy(
                    isYouTubeMusicConnected = false,
                    isPremiumVerified = false,
                    connectedAccountEmail = null,
                )
            }
        }
    }

    fun clearAuthError() {
        _uiState.value = _uiState.value.copy(authError = null)
    }
}
