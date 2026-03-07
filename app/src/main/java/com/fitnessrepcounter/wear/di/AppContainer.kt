package com.fitnessrepcounter.wear.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.fitnessrepcounter.wear.data.billing.GooglePlayBillingClient
import com.fitnessrepcounter.wear.data.billing.ProBillingClient
import com.fitnessrepcounter.wear.data.local.datastore.EntitlementDataStore
import com.fitnessrepcounter.wear.data.local.room.WorkoutDatabase
import com.fitnessrepcounter.wear.data.repository.EntitlementRepositoryImpl
import com.fitnessrepcounter.wear.data.repository.MotionRepositoryImpl
import com.fitnessrepcounter.wear.data.repository.WorkoutRepositoryImpl
import com.fitnessrepcounter.wear.data.sensor.MotionSensorManager
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.session.WorkoutSessionManager
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.fitnessrepcounter.wear.presentation.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: WorkoutDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            WorkoutDatabase::class.java,
            "fitness-rep-counter.db",
        ).fallbackToDestructiveMigration().build()
    }

    private val entitlementStore: EntitlementDataStore by lazy {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { appContext.preferencesDataStoreFile("entitlement.preferences_pb") },
        )
        EntitlementDataStore(dataStore)
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

    val workoutSessionManager: WorkoutSessionManager by lazy {
        WorkoutSessionManager()
    }

    val hapticsManager: HapticsManager by lazy {
        HapticsManager.fromContext(appContext)
    }

    val viewModelFactory: ViewModelFactory by lazy {
        ViewModelFactory(this)
    }
}
