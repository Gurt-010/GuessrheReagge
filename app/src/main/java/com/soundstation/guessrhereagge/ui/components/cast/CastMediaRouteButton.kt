package com.soundstation.guessrhereagge.ui.components.cast

import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.GoogleApiAvailability
import com.soundstation.guessrhereagge.R
import com.soundstation.guessrhereagge.ui.theme.ReggaeColors

/**
 * Always-visible Cast control: gold Compose icon on top of the official [MediaRouteButton].
 *
 * The underlying [MediaRouteButton] is often invisible in Compose (no tint / no theme /
 * hidden until routes are found). Tapping the icon delegates to [MediaRouteButton.performClick].
 */
@Composable
fun CastMediaRouteButton(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var routeButton by remember { mutableStateOf<MediaRouteButton?>(null) }

    val playServicesOk = remember {
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) ==
            com.google.android.gms.common.ConnectionResult.SUCCESS
    }

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (playServicesOk) {
            AndroidView(
                modifier = Modifier.size(1.dp),
                factory = { viewContext ->
                    val themedContext = ContextThemeWrapper(
                        viewContext,
                        androidx.appcompat.R.style.Theme_AppCompat,
                    )
                    MediaRouteButton(themedContext).apply {
                        with(density) {
                            minimumWidth = 48.dp.roundToPx()
                            minimumHeight = 48.dp.roundToPx()
                        }
                        runCatching {
                            CastButtonFactory.setUpMediaRouteButton(viewContext.applicationContext, this)
                        }
                        routeButton = this
                    }
                },
                update = { button ->
                    routeButton = button
                    runCatching {
                        CastButtonFactory.setUpMediaRouteButton(button.context.applicationContext, button)
                    }
                },
            )
        }

        IconButton(
            onClick = {
                val button = routeButton
                if (button != null && playServicesOk) {
                    button.performClick()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.cast_unavailable),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Cast,
                contentDescription = stringResource(R.string.cast_button_description),
                tint = ReggaeColors.SunshineGold,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** Initializes Google Cast; call from [android.app.Application.onCreate]. */
fun initializeCastFramework(applicationContext: android.content.Context) {
    runCatching {
        CastContext.getSharedInstance(applicationContext)
    }
}

/** Warms CastContext in activities that show the Cast button. */
fun warmUpCastContext(activity: android.app.Activity) {
    runCatching {
        CastContext.getSharedInstance(activity)
    }
}
