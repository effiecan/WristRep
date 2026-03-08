package com.fitnessrepcounter.wear.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.fitnessrepcounter.wear.data.billing.GooglePlayBillingClient
import com.fitnessrepcounter.wear.data.billing.ProBillingClient
import com.fitnessrepcounter.wear.data.local.datastore.EntitlementDataStore
import com.fitnessrepcounter.wear.data.local.datastore.SettingsDataStore
import com.fitnessrepcounter.wear.data.local.room.WorkoutDatabase
import com.fitnessrepcounter.wear.data.repository.EntitlementRepositoryImpl
import com.fitnessrepcounter.wear.data.repository.MotionRepositoryImpl
import com.fitnessrepcounter.wear.data.repository.SettingsRepositoryImpl
import com.fitnessrepcounter.wear.data.repository.WorkoutRepositoryImpl
import com.fitnessrepcounter.wear.data.repository.WorkoutRuntimeRepositoryImpl
import com.fitnessrepcounter.wear.data.sensor.MotionSensorManager
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import com.fitnessrepcounter.wear.domain.repository.SettingsRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRuntimeRepository
import com.fitnessrepcounter.wear.domain.session.WorkoutSessionManager
import com.fitnessrepcounter.wear.platform.AndroidWorkoutServiceController
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.fitnessrepcounter.wear.platform.WorkoutServiceController
import com.fitnessrepcounter.wear.presentation.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: WorkoutDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            WorkoutDatabase::class.java,
            "fitness-rep-counter.db",
        ).fallbackToDestructiveMigration().build()
    }

    private val entitlementStore: EntitlementDataStore by lazy {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = applicationScope,
            produceFile = { appContext.preferencesDataStoreFile("entitlement.preferences_pb") },
        )
        EntitlementDataStore(dataStore)
    }

    private val settingsStore: SettingsDataStore by lazy {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = applicationScope,
            produceFile = { appContext.preferencesDataStoreFile("settings.preferences_pb") },
        )
        SettingsDataStore(dataStore)
    }

    private val motionSensorManager: MotionSensorManager by lazy {
        MotionSensorManager(appContext)
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(database.workoutDao())
    }

    private val proBillingClient: ProBillingClient by lazy {
        GooglePlayBillingClient(appContext)
    }

    val entitlementRepository: EntitlementRepository by lazy {
        EntitlementRepositoryImpl(
            entitlementDataStore = entitlementStore,
            billingClient = proBillingClient,
        )
    }

    val motionRepository: MotionRepository by lazy {
        MotionRepositoryImpl(motionSensorManager)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(settingsStore)
    }

    val workoutSessionManager: WorkoutSessionManager by lazy {
        WorkoutSessionManager()
    }

    val hapticsManager: HapticsManager by lazy {
        HapticsManager.fromContext(appContext)
    }

    private val workoutServiceController: WorkoutServiceController by lazy {
        AndroidWorkoutServiceController(appContext)
    }

    val workoutRuntimeRepository: WorkoutRuntimeRepository by lazy {
        WorkoutRuntimeRepositoryImpl(
            workoutRepository = workoutRepository,
            entitlementRepository = entitlementRepository,
            motionRepository = motionRepository,
            settingsRepository = settingsRepository,
            workoutSessionManager = workoutSessionManager,
            workoutServiceController = workoutServiceController,
            repositoryScope = applicationScope,
        )
    }

    val viewModelFactory: ViewModelFactory by lazy {
        ViewModelFactory(this)
    }
}
