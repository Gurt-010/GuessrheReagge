package com.soundstation.guessrhereagge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.soundstation.guessrhereagge.cast.GameCastCoordinator
import com.soundstation.guessrhereagge.data.localization.LocaleManager
import com.soundstation.guessrhereagge.data.model.AppLanguage
import com.soundstation.guessrhereagge.data.preferences.GamePreferences
import com.soundstation.guessrhereagge.navigation.GuessrNavGraph
import com.soundstation.guessrhereagge.ui.components.cast.warmUpCastContext
import com.soundstation.guessrhereagge.ui.theme.ReggaeTheme
import kotlinx.coroutines.runBlocking

/** Provides [GameCastCoordinator] to Compose screens without tight Activity coupling. */
val LocalGameCastCoordinator = staticCompositionLocalOf<GameCastCoordinator?> { null }

class MainActivity : AppCompatActivity() {

    private lateinit var castCoordinator: GameCastCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLanguage = runBlocking {
            AppLanguage.fromCode(
                GamePreferences(this@MainActivity).getSettingsOnce().languageCode,
            )
        }
        val appliedLanguage = LocaleManager.currentLanguage()
        if (!LocaleManager.isLocaleApplied() || appliedLanguage != savedLanguage) {
            LocaleManager.applyLanguage(savedLanguage)
            return
        }

        castCoordinator = GameCastCoordinator(this)
        warmUpCastContext(this)
        castCoordinator.initialize()

        enableEdgeToEdge()
        setContent {
            ReggaeTheme {
                CompositionLocalProvider(LocalGameCastCoordinator provides castCoordinator) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                    ) {
                        val navController = rememberNavController()
                        GuessrNavGraph(navController = navController)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::castCoordinator.isInitialized) {
            castCoordinator.release()
        }
        super.onDestroy()
    }
}
