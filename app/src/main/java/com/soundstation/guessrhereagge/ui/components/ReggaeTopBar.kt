package com.soundstation.guessrhereagge.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.ui.components.buttons.ReggaeIconButton
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography

@Composable
fun ReggaeTopBar(
    title: String,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onNavigateBack != null) {
            ReggaeIconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = ReggaeColors.SunshineGold,
                )
            }
        } else {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(start = 52.dp),
            )
        }

        Text(
            text = title,
            style = ReggaeTypography.titleLarge.copy(
                color = ReggaeColors.TextGold,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        )

        actions()
    }
}
