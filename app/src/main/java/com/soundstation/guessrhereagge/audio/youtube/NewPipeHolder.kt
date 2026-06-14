package com.soundstation.guessrhereagge.audio.youtube

import org.schabi.newpipe.extractor.NewPipe

object NewPipeHolder {

    @Volatile
    private var initialized = false

    @Synchronized
    fun ensureInitialized() {
        if (initialized) return
        NewPipe.init(NewPipeDownloader.create())
        initialized = true
    }
}
