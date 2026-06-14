package com.soundstation.guessrhereagge.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.soundstation.guessrhereagge.ui.screens.GameScreen
import com.soundstation.guessrhereagge.ui.screens.MainScreen
import com.soundstation.guessrhereagge.ui.screens.SettingsScreen
import com.soundstation.guessrhereagge.ui.screens.SplashScreen
import com.soundstation.guessrhereagge.viewmodel.GameViewModel
import com.soundstation.guessrhereagge.viewmodel.SettingsViewModel

private const val TRANSITION_DURATION_MS = 400

@Composable
fun GuessrNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(TRANSITION_DURATION_MS)) + slideInHorizontally(
                initialOffsetX = { it / 4 },
                animationSpec = tween(TRANSITION_DURATION_MS),
            )
        },
        exitTransition = {
            fadeOut(tween(TRANSITION_DURATION_MS)) + slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(TRANSITION_DURATION_MS),
            )
        },
        popEnterTransition = {
            fadeIn(tween(TRANSITION_DURATION_MS)) + slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(TRANSITION_DURATION_MS),
            )
        },
        popExitTransition = {
            fadeOut(tween(TRANSITION_DURATION_MS)) + slideOutHorizontally(
                targetOffsetX = { it / 4 },
                animationSpec = tween(TRANSITION_DURATION_MS),
            )
        },
    ) {
        composable(
            route = NavRoutes.SPLASH,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = {
                fadeOut(tween(TRANSITION_DURATION_MS)) +
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(TRANSITION_DURATION_MS),
                    )
            },
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.MAIN) {
            MainScreen(
                onStartGame = { navController.navigate(NavRoutes.GAME) },
                onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) },
            )
        }

        composable(NavRoutes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = NavRoutes.GAME,
            enterTransition = {
                fadeIn(tween(TRANSITION_DURATION_MS)) +
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(TRANSITION_DURATION_MS),
                    )
            },
        ) {
            val application = LocalContext.current.applicationContext as Application
            val gameViewModel: GameViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { GameViewModel(application) }
                },
            )
            GameScreen(
                viewModel = gameViewModel,
                onNavigateBack = {
                    gameViewModel.stopGame()
                    navController.popBackStack()
                },
            )
        }
    }
}
