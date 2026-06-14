package com.soundstation.guessrhereagge.data.model

/**
 * Supported in-app languages mapped to Android resource qualifiers.
 */
enum class AppLanguage(val code: String, val localeTag: String) {
    DUTCH("nl", "nl"),
    ENGLISH("en", "en"),
    FRENCH("fr", "fr"),
    GERMAN("de", "de"),
    ;

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: DUTCH
    }
}
