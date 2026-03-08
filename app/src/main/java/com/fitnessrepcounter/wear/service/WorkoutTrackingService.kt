package com.fitnessrepcounter.wear.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.fitnessrepcounter.wear.MainActivity
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.FitnessRepCounterApplication
import com.fitnessrepcounter.wear.domain.model.WorkoutRuntimeEvent
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var app: FitnessRepCounterApplication

    override fun onCreate() {
        super.onCreate()
        app = application as FitnessRepCounterApplication
        ensureNotificationChannel()

        serviceScope.launch {
            app.appContainer.workoutRuntimeRepository.uiState
                .map { state -> state.toNotificationSnapshot() }
                .distinctUntilChanged()
                .collect { snapshot ->
                    if (!app.appContainer.workoutRuntimeRepository.hasActiveSession.value) {
                        stopForegroundAndSelf()
                        return@collect
                    }
                    val notification = buildNotification(snapshot)
                    ServiceCompat.startForeground(
                        this@WorkoutTrackingService,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
                    )
                    NotificationManagerCompat.from(this@WorkoutTrackingService).notify(NOTIFICATION_ID, notification)
                }
        }

        serviceScope.launch {
            app.appContainer.workoutRuntimeRepository.workoutEvents.collect { event ->
                when (event) {
                    WorkoutRuntimeEvent.CountdownTick -> app.appContainer.hapticsManager.performCountdownTick()
                    WorkoutRuntimeEvent.RepConfirmed -> app.appContainer.hapticsManager.performRepConfirmed()
                    WorkoutRuntimeEvent.SetFinished -> app.appContainer.hapticsManager.performSetFinished()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }

            ACTION_START,
            null,
            -> {
                if (!app.appContainer.workoutRuntimeRepository.hasActiveSession.value) {
                    stopForegroundAndSelf()
                    return START_NOT_STICKY
                }
                val notification = buildNotification(
                    app.appContainer.workoutRuntimeRepository.uiState.value.toNotificationSnapshot(),
                )
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
                )
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
                return START_STICKY
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(snapshot: WorkoutNotificationSnapshot): android.app.Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_workout_ongoing)
            .setContentTitle(getString(R.string.workout))
            .setContentText(snapshot.contentText)
            .setSubText(snapshot.subText)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setContentIntent(resumePendingIntent())

        if (app.appContainer.workoutRuntimeRepository.canExposeOngoingEntry.value) {
            OngoingActivity.Builder(this, NOTIFICATION_ID, builder)
                .setStaticIcon(Icon.createWithResource(this, R.drawable.ic_workout_ongoing))
                .setTouchIntent(resumePendingIntent())
                .setTitle(getString(R.string.workout))
                .setContentDescription(snapshot.contentText)
                .setStatus(buildOngoingStatus(snapshot))
                .build()
                .apply(applicationContext)
        }

        return builder.build()
    }

    private fun resumePendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_RESUME_WORKOUT, true)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_RESUME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.workout_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.workout_notification_channel_description)
            },
        )
    }

    private fun stopForegroundAndSelf() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildOngoingStatus(snapshot: WorkoutNotificationSnapshot): Status {
        return Status.Builder()
            .addTemplate(snapshot.contentText)
            .build()
    }

    private fun WorkoutUiState.toNotificationSnapshot(): WorkoutNotificationSnapshot {
        val content = when (currentStep) {
            WorkoutStep.READY -> getString(R.string.workout_status_ready_countdown, countdownValue.coerceAtLeast(1))
            WorkoutStep.ACTIVE -> getString(R.string.workout_status_active_reps, currentRepCount)
            WorkoutStep.END_SET_CONFIRMATION -> getString(R.string.set_complete)
            WorkoutStep.REST_TIMER -> getString(R.string.workout_status_rest, restSecondsRemaining)
            WorkoutStep.SUMMARY -> getString(R.string.summary)
            WorkoutStep.EXERCISE_SELECTION -> getString(R.string.workout)
        }
        val subtitle = selectedExercise?.let { getString(it.displayNameRes) } ?: getString(R.string.workout)
        return WorkoutNotificationSnapshot(
            contentText = content,
            subText = subtitle,
        )
    }

    companion object {
        private const val ACTION_START = "com.fitnessrepcounter.wear.service.action.START"
        private const val ACTION_STOP = "com.fitnessrepcounter.wear.service.action.STOP"
        private const val CHANNEL_ID = "workout_tracking"
        private const val NOTIFICATION_ID = 7001
        private const val REQUEST_CODE_RESUME = 9001
        const val EXTRA_RESUME_WORKOUT = "resume_workout"

        fun startIntent(context: Context): Intent {
            return Intent(context, WorkoutTrackingService::class.java).setAction(ACTION_START)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, WorkoutTrackingService::class.java).setAction(ACTION_STOP)
        }
    }
}

private data class WorkoutNotificationSnapshot(
    val contentText: String,
    val subText: String,
)
