package com.soundstation.guessrhereagge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors
import com.soundstation.guessrhereagge.ui.theme.ReggaeTypography

@Composable
fun ReggaeSwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = ReggaeTypography.titleLarge,
                color = ReggaeColors.TextPrimary,
            )
            description?.let {
                Text(
                    text = it,
                    style = ReggaeTypography.bodyMedium,
                    color = ReggaeColors.TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ReggaeColors.TextPrimary,
                checkedTrackColor = ReggaeColors.LionGreen,
                uncheckedThumbColor = ReggaeColors.TextPrimary.copy(alpha = 0.7f),
                uncheckedTrackColor = ReggaeColors.SurfaceElevated,
            ),
        )
    }
}
