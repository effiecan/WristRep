package com.fitnessrepcounter.wear.platform

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fitnessrepcounter.wear.service.WorkoutTrackingService

interface WorkoutServiceController {
    fun start()
    fun stop()
    fun areNotificationsEnabled(): Boolean
}

class AndroidWorkoutServiceController(
    context: Context,
) : WorkoutServiceController {
    private val appContext = context.applicationContext

    override fun start() {
        ContextCompat.startForegroundService(appContext, WorkoutTrackingService.startIntent(appContext))
    }

    override fun stop() {
        appContext.stopService(WorkoutTrackingService.stopIntent(appContext))
    }

    override fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(appContext).areNotificationsEnabled()
    }
}
