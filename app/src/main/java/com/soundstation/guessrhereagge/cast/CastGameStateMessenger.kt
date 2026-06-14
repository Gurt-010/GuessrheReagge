package com.soundstation.guessrhereagge.cast

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.framework.CastSession
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.viewmodel.GameUiState
import org.json.JSONObject

/**
 * Pushes live [GameUiState] to the Cast Custom Receiver so the TV renders gameplay.
 *
 * Requires a Custom Receiver App ID (not [DEFAULT_MEDIA_RECEIVER_APP_ID]) registered at
 * https://cast.google.com/publish/ pointing to [cast-receiver/index.html].
 */
class CastGameStateMessenger(
    private val context: Context,
) {
    private var session: CastSession? = null
    private var usingCustomReceiver: Boolean = false

    fun bind(session: CastSession, receiverAppId: String) {
        this.session = session
        usingCustomReceiver = receiverAppId != DEFAULT_MEDIA_RECEIVER_APP_ID
        if (!usingCustomReceiver) {
            Log.w(TAG, "Default media receiver active — game UI requires a custom Cast receiver App ID")
            return
        }
        runCatching {
            session.setMessageReceivedCallbacks(CastGameNamespaces.GAME_STATE) { _, _, _ -> }
        }.onFailure { error ->
            Log.e(TAG, "Failed to register Cast message callbacks", error)
        }
    }

    fun unbind() {
        runCatching {
            session?.removeMessageReceivedCallbacks(CastGameNamespaces.GAME_STATE)
        }
        session = null
    }

    fun usesCustomReceiver(): Boolean = usingCustomReceiver

    fun publish(state: GameUiState) {
        val activeSession = session ?: return
        if (!usingCustomReceiver || !activeSession.isConnected) return

        val payload = buildPayload(state)
        activeSession
            .sendMessage(CastGameNamespaces.GAME_STATE, payload)
            .setResultCallback { result ->
                if (!result.status.isSuccess) {
                    Log.w(TAG, "Game state send failed: ${result.status}")
                }
            }
    }

    private fun buildPayload(state: GameUiState): String {
        val res = context.resources
        val isEndless = state.totalRounds == GameSettings.ENDLESS_ROUNDS
        val progress = when {
            isEndless -> ((state.currentRound % 10).coerceAtLeast(1)).toFloat() / 10f
            state.totalRounds > 0 -> state.currentRound.toFloat() / state.totalRounds
            else -> 0f
        }

        return JSONObject().apply {
            put("phase", state.phase.name)
            put("currentRound", state.currentRound)
            put("totalRounds", state.totalRounds)
            put("isEndless", isEndless)
            put("progress", progress.toDouble())
            put("isMetadataRevealed", state.isMetadataRevealed)
            put("playbackState", state.playbackState.name)
            put("errorMessage", state.errorMessage)
            state.currentTrack?.let { track ->
                put("trackTitle", track.title)
                put("trackArtist", track.artist)
                put("releaseYear", track.releaseYear)
            }
            put(
                "labels",
                JSONObject().apply {
                    put("loading", res.getString(R.string.game_loading))
                    put("guessPrompt", res.getString(R.string.game_guess_prompt))
                    put("releaseYearLabel", res.getString(R.string.game_release_year_label))
                    put("finished", res.getString(R.string.game_finished))
                    put("roundProgress", res.getString(R.string.game_round_progress, 0, 0))
                    put("roundEndless", res.getString(R.string.game_round_endless, 0))
                    put("statusLoading", res.getString(R.string.game_status_loading))
                    put("statusPlaying", res.getString(R.string.game_status_playing))
                    put("statusPaused", res.getString(R.string.game_status_paused))
                    put("statusError", res.getString(R.string.game_status_error))
                    put("statusReady", res.getString(R.string.game_status_ready))
                    put("appTitle", res.getString(R.string.game_title))
                },
            )
        }.toString()
    }

    companion object {
        private const val TAG = "CastGameStateMessenger"
    }
}
