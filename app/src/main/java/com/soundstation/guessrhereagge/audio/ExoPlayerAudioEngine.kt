package com.soundstation.guessrhereagge.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExoPlayerAudioEngine(context: Context) : YouTubeMusicAudioEngine {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .build()
        .apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    _playbackState.value = when (state) {
                        Player.STATE_BUFFERING -> PlaybackState.LOADING
                        Player.STATE_READY -> if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                        Player.STATE_ENDED -> PlaybackState.STOPPED
                        else -> _playbackState.value
                    }
                    _durationMs.value = duration.coerceAtLeast(0L)
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (playbackState == Player.STATE_READY) {
                        _playbackState.value = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "ExoPlayer error: ${error.errorCodeName}", error)
                    _playbackState.value = PlaybackState.ERROR
                }
            })
        }

    init {
        scope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _currentPositionMs.value = player.currentPosition
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                }
                delay(POSITION_POLL_INTERVAL_MS)
            }
        }
    }

    override suspend fun play(streamUrl: String, requestHeaders: Map<String, String>) {
        withContext(Dispatchers.Main) {
            if (streamUrl.isBlank()) {
                _playbackState.value = PlaybackState.ERROR
                return@withContext
            }
            _playbackState.value = PlaybackState.LOADING
            player.stop()
            player.clearMediaItems()

            val userAgent = requestHeaders["User-Agent"] ?: DEFAULT_USER_AGENT
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(20_000)
                .setReadTimeoutMs(20_000)
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent(userAgent)
                .apply {
                    if (requestHeaders.isNotEmpty()) {
                        setDefaultRequestProperties(requestHeaders)
                    }
                }

            val mediaSource = DefaultMediaSourceFactory(appContext)
                .setDataSourceFactory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(streamUrl))

            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
        }
    }

    override fun pause() {
        player.pause()
        _playbackState.value = PlaybackState.PAUSED
    }

    override fun resume() {
        player.play()
        _playbackState.value = PlaybackState.PLAYING
    }

    override fun stop() {
        player.stop()
        player.clearMediaItems()
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
        _playbackState.value = PlaybackState.STOPPED
    }

    override fun release() {
        stop()
        scope.cancel()
        player.release()
        _playbackState.value = PlaybackState.IDLE
    }

    override fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    companion object {
        private const val TAG = "GuessrAudio"
        private const val POSITION_POLL_INTERVAL_MS = 250L
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
