package com.soundstation.guessrhereagge.data.localization

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.data.model.AppLanguage
import com.soundstation.guessrhereagge.data.model.AuthError
import com.soundstation.guessrhereagge.data.model.GameError

object LocaleManager {

    fun applyLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.localeTag),
        )
    }

    fun currentLanguage(): AppLanguage? {
        val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (tags.isNullOrEmpty()) return null
        val primary = tags.split(',').firstOrNull()?.substringBefore('-') ?: return null
        return AppLanguage.fromCode(primary)
    }

    fun isLocaleApplied(): Boolean =
        !AppCompatDelegate.getApplicationLocales().toLanguageTags().isNullOrEmpty()
}

fun AuthError.toMessage(context: Context): String = context.getString(toStringRes())

fun GameError.toMessage(context: Context): String = context.getString(toStringRes())

@StringRes
private fun AuthError.toStringRes(): Int = when (this) {
    AuthError.USERNAME_REQUIRED -> R.string.error_auth_username
    AuthError.PASSWORD_REQUIRED -> R.string.error_auth_password
    AuthError.INVALID_PASSWORD -> R.string.error_auth_invalid_password
}

@StringRes
private fun GameError.toStringRes(): Int = when (this) {
    GameError.YOUTUBE_NOT_CONNECTED -> R.string.error_youtube_not_connected
    GameError.NO_TRACKS_FOUND -> R.string.error_no_tracks
    GameError.STREAM_RESOLUTION_FAILED -> R.string.error_stream_resolution
}
