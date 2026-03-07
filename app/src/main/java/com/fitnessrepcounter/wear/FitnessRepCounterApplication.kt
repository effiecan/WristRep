package com.fitnessrepcounter.wear

import android.app.Application
import com.fitnessrepcounter.wear.di.AppContainer

class FitnessRepCounterApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
