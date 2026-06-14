package com.soundstation.guessrhereagge.data.model

/**
 * Represents a music track fetched from a YouTube Music playlist.
 * Metadata is hidden from players during gameplay until explicitly revealed.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val releaseYear: Int,
    /** Resolved at playback from YouTube Music. */
    val streamUrl: String = "",
    /** Optional YouTube video id; searched automatically when omitted. */
    val youtubeVideoId: String? = null,
    /** Search query from YouTube Music link for accurate stream lookup. */
    val youtubeSearchQuery: String? = null,
    val genre: String = MusicGenre.REGGAE.name,
)
