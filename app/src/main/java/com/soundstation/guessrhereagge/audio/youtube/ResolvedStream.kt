package com.soundstation.guessrhereagge.audio.youtube

data class ResolvedStream(
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
)

class StreamResolutionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
