package com.soundstation.guessrhereagge.cast

import android.content.Context
import android.os.Bundle
import android.graphics.Color
import android.view.Display
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.soundstation.guessrhereagge.ui.screens.GameTvContent
import com.soundstation.guessrhereagge.ui.theme.ReggaeTheme
import com.soundstation.guessrhereagge.viewmodel.GameUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Renders the game UI on the external TV display via the Presentation API.
 *
 * Uses the hosting activity as [LifecycleOwner] so Compose state collection stays active.
 */
class GamePresentation(
    context: Context,
    display: Display,
    private val uiStateFlow: StateFlow<GameUiState>,
    private val lifecycleOwner: LifecycleOwner,
) : android.app.Presentation(context, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setBackgroundDrawable(null)
        }

        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

        val composeView = ComposeView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            )
            setViewTreeLifecycleOwner(lifecycleOwner)
            if (lifecycleOwner is SavedStateRegistryOwner) {
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            }
        }

        root.addView(composeView)
        setContentView(root)

        composeView.setContent {
            val uiState by uiStateFlow.collectAsState()
            ReggaeTheme {
                AspectRatio16x9Container {
                    GameTvContent(uiState = uiState)
                }
            }
        }
    }

    companion object {
        const val ASPECT_RATIO_16_9 = 16f / 9f
    }
}
