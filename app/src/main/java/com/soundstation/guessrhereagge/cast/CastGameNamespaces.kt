package com.soundstation.guessrhereagge.cast

/** Custom Cast namespace for synchronizing game UI to the TV receiver. */
object CastGameNamespaces {
    const val GAME_STATE = "urn:x-cast:com.soundstation.guessrhereagge.gamestate"
}

/** Google Default Media Receiver — audio only, no custom game UI. */
const val DEFAULT_MEDIA_RECEIVER_APP_ID = "CC1AD845"
