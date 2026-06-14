package com.soundstation.guessrhereagge.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.ui.components.background.ReggaeBackground
import com.soundstation.guessrhereagge.ui.components.logo.ReggaeLogo
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 2_000L
private const val FADE_IN_DURATION_MS = 800

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var startAnimation by remember { mutableStateOf(true) }
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = FADE_IN_DURATION_MS),
        label = "logoFadeIn",
    )

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onSplashFinished()
    }

    ReggaeBackground(animateGradient = true) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .alpha(logoAlpha),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            ReggaeLogo(
                size = 132.dp,
                contentDescription = stringResource(R.string.app_logo_content_description),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = ReggaeTypography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = ReggaeColors.TextPrimary,
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                style = ReggaeTypography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = ReggaeColors.SunshineGold,
                trackColor = ReggaeColors.LionGreen.copy(alpha = 0.3f),
                strokeWidth = 3.dp,
            )
        }
    }
}
