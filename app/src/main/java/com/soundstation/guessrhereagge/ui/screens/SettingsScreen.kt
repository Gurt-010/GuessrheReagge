package com.soundstation.guessrhereagge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.data.model.AppLanguage
import com.soundstation.guessrhereagge.data.model.GameSettings
import com.soundstation.guessrhereagge.data.model.MusicGenre
import com.soundstation.guessrhereagge.ui.components.ReggaeCard
import com.soundstation.guessrhereagge.ui.components.ReggaeSwitchRow
import com.soundstation.guessrhereagge.ui.components.ReggaeTopBar
import com.soundstation.guessrhereagge.ui.components.background.ReggaeBackground
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButton
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeButtonVariant
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeIconButton
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography
import com.soundstation.guessrhereagge.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.authError, uiState.showLoginDialog) {
        if (uiState.showLoginDialog) return@LaunchedEffect
        uiState.authError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearAuthError()
        }
    }

    ReggaeBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReggaeTopBar(
                    title = stringResource(R.string.settings),
                    onNavigateBack = onNavigateBack,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AccountAndAudioCard(
                        isConnected = uiState.settings.isYouTubeMusicConnected,
                        isPremium = uiState.settings.isPremiumVerified,
                        accountEmail = uiState.settings.connectedAccountEmail,
                        volume = uiState.settings.volume,
                        onConnect = viewModel::openLoginDialog,
                        onDisconnect = viewModel::disconnectYouTubeMusic,
                        onVolumeChange = viewModel::setVolume,
                    )

                    GameOptionsCard(
                        settings = uiState.settings,
                        onRoundPreset = viewModel::setRoundPreset,
                        onEndlessChange = viewModel::setEndlessRounds,
                        onGuessTimerEnabled = viewModel::setGuessTimerEnabled,
                        onGuessTimerSeconds = viewModel::setGuessTimerSeconds,
                        onPreRollChange = viewModel::setAudioPreRollEnabled,
                        onGenreToggle = viewModel::toggleGenre,
                    )

                    LanguageAndInfoCard(
                        selectedLanguage = AppLanguage.fromCode(uiState.settings.languageCode),
                        isAboutExpanded = uiState.isAboutExpanded,
                        onLanguageSelected = viewModel::setLanguage,
                        onToggleAbout = viewModel::toggleAboutExpanded,
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (uiState.showLoginDialog) {
        YouTubeMusicLoginDialog(
            isLoading = uiState.isAuthenticating,
            errorMessage = uiState.authError,
            onDismiss = viewModel::dismissLoginDialog,
            onLogin = viewModel::connectYouTubeMusic,
        )
    }
}

@Composable
private fun AccountAndAudioCard(
    isConnected: Boolean,
    isPremium: Boolean,
    accountEmail: String?,
    volume: Float,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onVolumeChange: (Float) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.settings_card_account_audio)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.settings_youtube_music),
                    style = ReggaeTypography.titleLarge,
                    color = ReggaeColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.settings_youtube_music_description),
                    style = ReggaeTypography.bodyMedium,
                    color = ReggaeColors.TextPrimary.copy(alpha = 0.75f),
                )
                if (isConnected && isPremium) {
                    Text(
                        text = stringResource(R.string.settings_connected),
                        style = ReggaeTypography.titleMedium,
                        color = ReggaeColors.LionGreen,
                    )
                    accountEmail?.let { email ->
                        Text(text = email, style = ReggaeTypography.bodyLarge, color = ReggaeColors.TextGold)
                    }
                    ReggaeButton(
                        text = stringResource(R.string.settings_disconnect),
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ReggaeButtonVariant.Outline,
                    )
                } else {
                    ReggaeButton(
                        text = stringResource(R.string.settings_connect_youtube),
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ReggaeButtonVariant.Primary,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_volume),
                    style = ReggaeTypography.titleLarge,
                    color = ReggaeColors.TextPrimary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = ReggaeColors.SunshineGold,
                            activeTrackColor = ReggaeColors.LionGreen,
                            inactiveTrackColor = ReggaeColors.SurfaceElevated,
                        ),
                    )
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        style = ReggaeTypography.bodyLarge,
                        color = ReggaeColors.TextGold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameOptionsCard(
    settings: GameSettings,
    onRoundPreset: (Int) -> Unit,
    onEndlessChange: (Boolean) -> Unit,
    onGuessTimerEnabled: (Boolean) -> Unit,
    onGuessTimerSeconds: (Int) -> Unit,
    onPreRollChange: (Boolean) -> Unit,
    onGenreToggle: (MusicGenre) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.settings_card_game_options)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.settings_game_options),
                style = ReggaeTypography.titleMedium,
                color = ReggaeColors.TextGold,
            )

            Text(
                text = if (settings.isEndlessRounds) {
                    stringResource(R.string.settings_rounds_endless_label)
                } else {
                    stringResource(R.string.settings_rounds_label, settings.numberOfRounds)
                },
                style = ReggaeTypography.bodyLarge,
                color = ReggaeColors.TextPrimary,
            )

            Text(
                text = stringResource(R.string.settings_rounds_preset_label),
                style = ReggaeTypography.titleLarge,
                color = ReggaeColors.TextPrimary,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GameSettings.ROUND_PRESETS.forEach { preset ->
                    PresetChip(
                        label = preset.toString(),
                        selected = !settings.isEndlessRounds && settings.numberOfRounds == preset,
                        onClick = { onRoundPreset(preset) },
                    )
                }
                PresetChip(
                    label = stringResource(R.string.settings_rounds_endless),
                    selected = settings.isEndlessRounds,
                    onClick = { onEndlessChange(true) },
                )
            }

            ReggaeSwitchRow(
                title = stringResource(R.string.settings_guess_timer),
                description = stringResource(R.string.settings_guess_timer_description),
                checked = settings.isGuessTimerEnabled,
                onCheckedChange = onGuessTimerEnabled,
            )

            AnimatedVisibility(visible = settings.isGuessTimerEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_guess_timer_duration),
                        style = ReggaeTypography.titleLarge,
                        color = ReggaeColors.TextPrimary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GameSettings.GUESS_TIMER_OPTIONS.forEach { seconds ->
                            PresetChip(
                                label = stringResource(R.string.settings_timer_seconds, seconds),
                                selected = settings.guessTimerSeconds == seconds,
                                onClick = { onGuessTimerSeconds(seconds) },
                            )
                        }
                    }
                }
            }

            ReggaeSwitchRow(
                title = stringResource(R.string.settings_pre_roll),
                description = stringResource(R.string.settings_pre_roll_description),
                checked = settings.isAudioPreRollEnabled,
                onCheckedChange = onPreRollChange,
            )

            Text(
                text = stringResource(R.string.settings_genres_label),
                style = ReggaeTypography.titleLarge,
                color = ReggaeColors.TextPrimary,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MusicGenre.entries.forEach { genre ->
                    PresetChip(
                        label = stringResource(genre.labelRes),
                        selected = genre.storageKey in settings.selectedGenres,
                        onClick = { onGenreToggle(genre) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageAndInfoCard(
    selectedLanguage: AppLanguage,
    isAboutExpanded: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleAbout: () -> Unit,
) {
    val context = LocalContext.current

    SettingsSectionCard(title = stringResource(R.string.settings_card_language_info)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.settings_language),
                style = ReggaeTypography.titleLarge,
                color = ReggaeColors.TextPrimary,
            )
            Text(
                text = stringResource(R.string.settings_language_description),
                style = ReggaeTypography.bodyMedium,
                color = ReggaeColors.TextPrimary.copy(alpha = 0.75f),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LanguageChip(AppLanguage.DUTCH, stringResource(R.string.language_dutch), selectedLanguage, onLanguageSelected)
                LanguageChip(AppLanguage.ENGLISH, stringResource(R.string.language_english), selectedLanguage, onLanguageSelected)
                LanguageChip(AppLanguage.FRENCH, stringResource(R.string.language_french), selectedLanguage, onLanguageSelected)
                LanguageChip(AppLanguage.GERMAN, stringResource(R.string.language_german), selectedLanguage, onLanguageSelected)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleAbout),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_about_title),
                        style = ReggaeTypography.titleLarge,
                        color = ReggaeColors.TextGold,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (isAboutExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ReggaeColors.SunshineGold,
                    )
                }

                AnimatedVisibility(
                    visible = isAboutExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_about_story),
                            style = ReggaeTypography.bodyLarge,
                            color = ReggaeColors.TextPrimary.copy(alpha = 0.9f),
                        )
                        Text(
                            text = stringResource(R.string.settings_about_credits),
                            style = ReggaeTypography.titleMedium,
                            color = ReggaeColors.TextGold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ReggaeIconButton(
                                onClick = {
                                    val url = context.getString(R.string.settings_about_github_url)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = stringResource(R.string.settings_github),
                                    tint = ReggaeColors.SunshineGold,
                                )
                            }
                            ReggaeIconButton(
                                onClick = {
                                    val email = context.getString(R.string.settings_about_email)
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")),
                                    )
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = stringResource(R.string.settings_email),
                                    tint = ReggaeColors.SunshineGold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    language: AppLanguage,
    label: String,
    selected: AppLanguage,
    onSelected: (AppLanguage) -> Unit,
) {
    PresetChip(
        label = label,
        selected = language == selected,
        onClick = { onSelected(language) },
    )
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label, style = ReggaeTypography.labelLarge, maxLines = 1)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ReggaeColors.LionGreen.copy(alpha = 0.35f),
            selectedLabelColor = ReggaeColors.TextPrimary,
            containerColor = ReggaeColors.SurfaceDark,
            labelColor = ReggaeColors.TextPrimary.copy(alpha = 0.85f),
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = ReggaeColors.LionGreen.copy(alpha = 0.4f),
            selectedBorderColor = ReggaeColors.SunshineGold,
        ),
    )
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    ReggaeCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = ReggaeTypography.headlineMedium,
                color = ReggaeColors.TextGold,
            )
            content()
        }
    }
}

@Composable
private fun YouTubeMusicLoginDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onLogin: (username: String, password: String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.settings_login_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.settings_login_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.settings_username_label)) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.settings_password_label)) },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) {
                                    stringResource(R.string.settings_hide_password)
                                } else {
                                    stringResource(R.string.settings_show_password)
                                },
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (!isLoading) onLogin(username, password)
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onLogin(username, password) }, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).height(18.dp), strokeWidth = 2.dp)
                }
                Text(stringResource(R.string.settings_login_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.settings_login_cancel))
            }
        },
    )
}
