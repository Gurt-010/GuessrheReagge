package com.soundstation.guessrhereagge.cast

import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.soundstation.guessrhereagge.audio.YouTubeMusicAudioEngine
import com.soundstation.guessrhereagge.data.model.Track
import com.soundstation.guessrhereagge.viewmodel.GameUiState

/**
 * Routes game audio to the TV when a Cast session is active.
 *
 * Local ExoPlayer output is muted on the phone; playback is mirrored to the
 * Cast receiver via [com.google.android.gms.cast.framework.media.RemoteMediaClient].
 */
class AudioCastRouter(
    private val audioEngine: YouTubeMusicAudioEngine,
) {

    private var savedLocalVolume: Float = 1f
    private var isCastAudioActive: Boolean = false

    /** Mute local speaker and attempt remote playback on the Cast receiver. */
    fun routeToTv(
        session: CastSession,
        streamUrl: String,
        track: Track?,
        gameState: GameUiState? = null,
    ) {
        savedLocalVolume = savedLocalVolume.coerceAtLeast(audioEngine.playbackState.value.let { 1f })
        audioEngine.setVolume(0f)
        isCastAudioActive = true

        val remoteClient = session.remoteMediaClient ?: run {
            Log.w(TAG, "No RemoteMediaClient available")
            return
        }

        if (streamUrl.isBlank()) return

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            if (gameState != null && !gameState.isMetadataRevealed) {
                putString(MediaMetadata.KEY_TITLE, gameState.currentTrack?.title ?: "Guessrhe Reagge")
                putString(
                    MediaMetadata.KEY_SUBTITLE,
                    "Round ${gameState.currentRound} — Guess the year",
                )
                putString(MediaMetadata.KEY_ARTIST, "Guessrhe Reagge")
            } else {
                putString(MediaMetadata.KEY_TITLE, track?.title ?: "Guessrhe Reagge")
                putString(MediaMetadata.KEY_ARTIST, track?.artist ?: "")
            }
        }

        val mediaInfo = MediaInfo.Builder(streamUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("application/x-mpegURL")
            .setMetadata(metadata)
            .build()

        remoteClient.load(mediaInfo, true, 0).setResultCallback { result ->
            if (!result.status.isSuccess) {
                Log.w(TAG, "Remote media load failed: ${result.status}")
            }
        }
    }

    /** Restore local audio output after Cast disconnect. */
    fun restoreLocalPlayback(session: CastSession?) {
        session?.remoteMediaClient?.stop()
        if (isCastAudioActive) {
            audioEngine.setVolume(savedLocalVolume.coerceIn(0f, 1f))
            isCastAudioActive = false
        }
    }

    fun rememberUserVolume(volume: Float) {
        if (!isCastAudioActive) {
            savedLocalVolume = volume.coerceIn(0f, 1f)
        }
    }

    fun isRoutingToTv(): Boolean = isCastAudioActive

    companion object {
        private const val TAG = "AudioCastRouter"
    }
}
