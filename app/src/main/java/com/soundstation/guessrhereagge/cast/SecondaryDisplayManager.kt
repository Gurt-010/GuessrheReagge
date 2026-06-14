package com.soundstation.guessrhereagge.cast

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display

/**
 * Monitors [DisplayManager] for secondary displays (HDMI, wireless display, Cast-backed routes)
 * suitable for [GamePresentation].
 */
class SecondaryDisplayManager(
    context: Context,
    private val onDisplayAvailable: (Display) -> Unit,
    private val onDisplayRemoved: () -> Unit,
) {
    private val displayManager =
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            findPresentationDisplay()?.let(onDisplayAvailable)
        }

        override fun onDisplayRemoved(displayId: Int) {
            onDisplayRemoved()
        }

        override fun onDisplayChanged(displayId: Int) {
            findPresentationDisplay()?.let(onDisplayAvailable)
        }
    }

    fun start() {
        displayManager.registerDisplayListener(displayListener, mainHandler)
        findPresentationDisplay()?.let(onDisplayAvailable)
    }

    fun stop() {
        displayManager.unregisterDisplayListener(displayListener)
    }

    /**
     * Returns the best secondary display for game Presentation (non-default, valid state).
     */
    fun findPresentationDisplay(): Display? {
        return displayManager.displays
            .filter { it.displayId != Display.DEFAULT_DISPLAY }
            .filter { it.state == Display.STATE_ON }
            .maxByOrNull { it.mode?.physicalWidth ?: 0 }
            ?: run {
                Log.d(TAG, "No secondary display detected")
                null
            }
    }

    companion object {
        private const val TAG = "SecondaryDisplayMgr"
    }
}
