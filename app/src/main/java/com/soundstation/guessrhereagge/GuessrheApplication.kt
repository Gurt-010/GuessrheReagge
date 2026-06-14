package com.soundstation.guessrhereagge

import android.app.Application
import com.soundstation.guessrhereagge.ui.components.cast.initializeCastFramework

class GuessrheApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeCastFramework(this)
    }
}
