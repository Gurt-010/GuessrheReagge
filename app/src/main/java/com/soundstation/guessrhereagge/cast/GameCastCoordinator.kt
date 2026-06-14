package com.soundstation.guessrhereagge.cast

import android.util.Log
import android.view.Display
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.viewmodel.GameViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central coordinator for Cast sessions, Presentation lifecycle, and dual-screen mode.
 *
 * Game UI on Chromecast is rendered by the Custom Web Receiver ([cast-receiver/index.html])
 * via [CastGameStateMessenger]. [GamePresentation] is used when a local secondary display
 * (HDMI / wireless display) is available.
 */
class GameCastCoordinator(
    private val activity: FragmentActivity,
) {
    private val _connectionState = MutableStateFlow(CastConnectionState.DISCONNECTED)
    val connectionState: StateFlow<CastConnectionState> = _connectionState.asStateFlow()

    private val gameStateMessenger = CastGameStateMessenger(activity.applicationContext)
    private val receiverAppId: String = activity.getString(R.string.cast_app_id)

    private var castContext: CastContext? = null
    private var sessionManager: SessionManager? = null
    private var sessionConsumer: CastSessionConsumer? = null
    private var secondaryDisplayManager: SecondaryDisplayManager? = null

    private var gamePresentation: GamePresentation? = null
    private var activeCastSession: CastSession? = null
    private var boundViewModel: GameViewModel? = null
    private var stateCollectionJob: Job? = null

    private var lastStreamUrl: String = ""
    private var disconnectListener: (() -> Unit)? = null
    private var warnedDefaultReceiver: Boolean = false

    val isCastMode: Boolean
        get() = _connectionState.value == CastConnectionState.CONNECTED

    fun initialize() {
        runCatching {
            castContext = CastContext.getSharedInstance(activity)
            sessionManager = castContext?.sessionManager
            sessionConsumer = CastSessionConsumer(this).also { consumer ->
                sessionManager?.addSessionManagerListener(consumer, CastSession::class.java)
            }
            secondaryDisplayManager = SecondaryDisplayManager(
                context = activity,
                onDisplayAvailable = { display -> showPresentation(display) },
                onDisplayRemoved = { handleSecondaryDisplayRemoved() },
            ).also { it.start() }

            sessionManager?.currentCastSession?.let { onCastSessionStarted(it) }
        }.onFailure { error ->
            Log.e(TAG, "Cast initialization failed", error)
            _connectionState.value = CastConnectionState.ERROR
        }
    }

    fun release() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        gameStateMessenger.unbind()
        sessionConsumer?.let { consumer ->
            sessionManager?.removeSessionManagerListener(consumer, CastSession::class.java)
        }
        secondaryDisplayManager?.stop()
        dismissPresentation()
        activeCastSession?.let { session ->
            boundViewModel?.getAudioCastRouter()?.restoreLocalPlayback(session)
        }
        activeCastSession = null
        sessionConsumer = null
        sessionManager = null
        castContext = null
        boundViewModel = null
    }

    fun bindGameViewModel(viewModel: GameViewModel) {
        boundViewModel = viewModel
        startStateCollection(viewModel)
        if (isCastMode) {
            refreshPresentationContent()
            activeCastSession?.let { session ->
                routeCurrentAudio(session, viewModel)
                gameStateMessenger.publish(viewModel.uiState.value)
            }
        }
    }

    fun unbindGameViewModel() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        boundViewModel = null
    }

    fun setOnUnexpectedDisconnectListener(listener: (() -> Unit)?) {
        disconnectListener = listener
    }

    fun updateStreamUrl(url: String) {
        lastStreamUrl = url
        activeCastSession?.let { session ->
            boundViewModel?.let { routeCurrentAudio(session, it) }
        }
    }

    internal fun onCastConnecting() {
        _connectionState.value = CastConnectionState.CONNECTING
    }

    internal fun onCastSessionStarted(session: CastSession) {
        activeCastSession = session
        _connectionState.value = CastConnectionState.CONNECTED

        gameStateMessenger.bind(session, receiverAppId)
        maybeWarnDefaultReceiver()

        secondaryDisplayManager?.findPresentationDisplay()?.let { showPresentation(it) }
        boundViewModel?.let { viewModel ->
            routeCurrentAudio(session, viewModel)
            gameStateMessenger.publish(viewModel.uiState.value)
            startStateCollection(viewModel)
        }
    }

    internal fun onCastSessionEnded(wasUnexpected: Boolean) {
        val session = activeCastSession
        activeCastSession = null
        gameStateMessenger.unbind()
        boundViewModel?.getAudioCastRouter()?.restoreLocalPlayback(session)
        dismissPresentation()

        if (wasUnexpected) {
            boundViewModel?.onCastDisconnected()
            disconnectListener?.invoke()
        }
        _connectionState.value = CastConnectionState.DISCONNECTED
    }

    internal fun onCastSessionFailed(error: Int) {
        Log.w(TAG, "Cast failed with error code $error")
        _connectionState.value = CastConnectionState.ERROR
    }

    private fun startStateCollection(viewModel: GameViewModel) {
        stateCollectionJob?.cancel()
        stateCollectionJob = activity.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                gameStateMessenger.publish(state)
            }
        }
    }

    private fun maybeWarnDefaultReceiver() {
        if (warnedDefaultReceiver || receiverAppId != DEFAULT_MEDIA_RECEIVER_APP_ID) return
        warnedDefaultReceiver = true
        Toast.makeText(
            activity,
            activity.getString(R.string.cast_custom_receiver_required),
            Toast.LENGTH_LONG,
        ).show()
    }

    private fun routeCurrentAudio(session: CastSession, viewModel: GameViewModel) {
        val state = viewModel.uiState.value
        val track = state.currentTrack
        val url = lastStreamUrl.ifBlank { track?.streamUrl.orEmpty() }
        if (url.isNotBlank()) {
            viewModel.getAudioCastRouter().routeToTv(session, url, track, state)
        }
    }

    private fun showPresentation(display: Display) {
        val viewModel = boundViewModel ?: return
        if (gamePresentation?.display?.displayId == display.displayId && gamePresentation?.isShowing == true) {
            return
        }

        dismissPresentation()

        runCatching {
            gamePresentation = GamePresentation(
                context = activity,
                display = display,
                uiStateFlow = viewModel.uiState,
                lifecycleOwner = activity,
            ).also { presentation ->
                presentation.show()
            }
            if (_connectionState.value != CastConnectionState.CONNECTED &&
                _connectionState.value != CastConnectionState.CONNECTING
            ) {
                _connectionState.value = CastConnectionState.CONNECTED
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to show Presentation", error)
        }
    }

    private fun dismissPresentation() {
        runCatching {
            gamePresentation?.dismiss()
        }
        gamePresentation = null
    }

    private fun handleSecondaryDisplayRemoved() {
        dismissPresentation()
        if (activeCastSession == null) {
            handleUnexpectedDisconnect()
        }
    }

    private fun handleUnexpectedDisconnect() {
        if (_connectionState.value == CastConnectionState.DISCONNECTED) return
        _connectionState.value = CastConnectionState.DISCONNECTED
        boundViewModel?.onCastDisconnected()
        disconnectListener?.invoke()
    }

    private fun refreshPresentationContent() {
        secondaryDisplayManager?.findPresentationDisplay()?.let { showPresentation(it) }
    }

    companion object {
        private const val TAG = "GameCastCoordinator"
    }
}
