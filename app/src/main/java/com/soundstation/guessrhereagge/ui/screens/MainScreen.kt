package com.soundstation.guessrhereagge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.ui.components.background.ReggaeBackground
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButton
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButtonVariant
import com.soundstation.guessrhereagge.ui.components.cast.CastMediaRouteButton
import com.soundstation.guessrhereagge.ui.components.logo.ReggaeLogo
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    onStartGame: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showStart by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showLogo = true
        delay(120)
        showTitle = true
        delay(140)
        showStart = true
        delay(120)
        showSettings = true
    }

    ReggaeBackground(animateGradient = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End,
            ) {
                CastMediaRouteButton()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AnimatedVisibility(
                    visible = showLogo,
                    enter = slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
                ) {
                    ReggaeLogo(
                        size = 148.dp,
                        contentDescription = stringResource(R.string.app_logo_content_description),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                AnimatedVisibility(
                    visible = showTitle,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.game_title),
                            style = ReggaeTypography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = ReggaeColors.TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = stringResource(R.string.game_subtitle),
                            style = ReggaeTypography.titleMedium,
                            color = ReggaeColors.TextGold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))

                Column(
                    modifier = Modifier.widthIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AnimatedVisibility(
                        visible = showStart,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                        ),
                    ) {
                        ReggaeButton(
                            text = stringResource(R.string.start_game),
                            onClick = onStartGame,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ReggaeButtonVariant.Primary,
                        )
                    }

                    AnimatedVisibility(
                        visible = showSettings,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                        ),
                    ) {
                        ReggaeButton(
                            text = stringResource(R.string.settings),
                            onClick = onOpenSettings,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ReggaeButtonVariant.Outline,
                        )
                    }
                }
            }
        }
    }
}
