package com.soundstation.guessrhereagge.cast

import android.util.Log
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

/**
 * Listens to Google Cast session lifecycle events and delegates to [GameCastCoordinator].
 *
 * Registered on the Cast [com.google.android.gms.cast.framework.SessionManager] during
 * activity creation and removed on destroy to avoid leaks.
 */
class CastSessionConsumer(
    private val coordinator: GameCastCoordinator,
) : SessionManagerListener<CastSession> {

    override fun onSessionStarting(session: CastSession) {
        Log.d(TAG, "Cast session starting")
        coordinator.onCastConnecting()
    }

    override fun onSessionStarted(session: CastSession, sessionId: String) {
        Log.d(TAG, "Cast session started: $sessionId")
        coordinator.onCastSessionStarted(session)
    }

    override fun onSessionStartFailed(session: CastSession, error: Int) {
        Log.w(TAG, "Cast session start failed: error=$error")
        coordinator.onCastSessionFailed(error)
    }

    override fun onSessionEnding(session: CastSession) {
        Log.d(TAG, "Cast session ending")
    }

    override fun onSessionEnded(session: CastSession, error: Int) {
        Log.d(TAG, "Cast session ended: error=$error")
        coordinator.onCastSessionEnded(wasUnexpected = error != 0)
    }

    override fun onSessionResuming(session: CastSession, sessionId: String) {
        Log.d(TAG, "Cast session resuming: $sessionId")
        coordinator.onCastConnecting()
    }

    override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
        Log.d(TAG, "Cast session resumed (suspended=$wasSuspended)")
        coordinator.onCastSessionStarted(session)
    }

    override fun onSessionResumeFailed(session: CastSession, error: Int) {
        Log.w(TAG, "Cast session resume failed: error=$error")
        coordinator.onCastSessionFailed(error)
    }

    override fun onSessionSuspended(session: CastSession, reason: Int) {
        Log.w(TAG, "Cast session suspended: reason=$reason")
        coordinator.onCastSessionEnded(wasUnexpected = true)
    }

    companion object {
        private const val TAG = "CastSessionConsumer"
    }
}
