package com.soundstation.guessrhereagge.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.soundstation.guessrhereagge.R

/**
 * Supplies Cast SDK configuration. Registered via AndroidManifest meta-data.
 *
 * Set [R.string.cast_app_id] to your Custom Receiver App ID from
 * https://cast.google.com/publish/ (host [cast-receiver/index.html] over HTTPS).
 * The default media receiver ID only supports audio — not the game UI.
 */
class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(
                "com.soundstation.guessrhereagge.MainActivity",
            )
            .build()

        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .build()

        return CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_app_id))
            .setCastMediaOptions(mediaOptions)
            .setEnableReconnectionService(true)
            .setStopReceiverApplicationWhenEndingSession(false)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
