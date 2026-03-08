package com.fitnessrepcounter.wear

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.fitnessrepcounter.wear.navigation.AppNavGraph
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.service.WorkoutTrackingService
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import androidx.wear.ambient.AmbientLifecycleObserver
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private lateinit var appContainer: com.fitnessrepcounter.wear.di.AppContainer
    private lateinit var ambientLifecycleObserver: AmbientLifecycleObserver
    private val resumeWorkoutRequests = MutableStateFlow(0)
    private val ambientModeState = MutableStateFlow(AmbientModeState())
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        appContainer.workoutRuntimeRepository.onNotificationPermissionPromptHandled(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appContainer = (application as FitnessRepCounterApplication).appContainer
        applySavedLocale()
        super.onCreate(savedInstanceState)
        ambientLifecycleObserver = AmbientLifecycleObserver(this, mainExecutor, ambientLifecycleCallback)
        lifecycle.addObserver(ambientLifecycleObserver)
        handleResumeIntent(intent)
        observeLanguageChanges()
        observeNotificationPermissionRequests()

        setContent {
            FitnessRepCounterTheme {
                AppNavGraph(
                    viewModelFactory = appContainer.viewModelFactory,
                    workoutRuntimeRepository = appContainer.workoutRuntimeRepository,
                    resumeWorkoutRequests = resumeWorkoutRequests,
                    ambientModeState = ambientModeState,
                )
            }
        }
    }

    override fun onDestroy() {
        if (::ambientLifecycleObserver.isInitialized) {
            lifecycle.removeObserver(ambientLifecycleObserver)
        }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleResumeIntent(intent)
    }

    private fun applySavedLocale() {
        val languageTag = runBlocking {
            appContainer.settingsRepository.currentSelectedLanguageTag()
        }
        AppCompatDelegate.setApplicationLocales(languageTag.toLocaleListCompat())
    }

    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            appContainer.settingsRepository.observeSettings()
                .map { it.selectedLanguageTag }
                .distinctUntilChanged()
                .collect { languageTag ->
                    AppCompatDelegate.setApplicationLocales(languageTag.toLocaleListCompat())
                }
        }
    }

    private fun observeNotificationPermissionRequests() {
        lifecycleScope.launch {
            appContainer.workoutRuntimeRepository.shouldRequestNotificationPermission.collect { shouldRequest ->
                if (!shouldRequest) return@collect
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    appContainer.workoutRuntimeRepository.onNotificationPermissionPromptHandled(true)
                    return@collect
                }
                val permissionState = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
                if (permissionState == PackageManager.PERMISSION_GRANTED) {
                    appContainer.workoutRuntimeRepository.onNotificationPermissionPromptHandled(true)
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun handleResumeIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(WorkoutTrackingService.EXTRA_RESUME_WORKOUT, false) == true) {
            resumeWorkoutRequests.value = resumeWorkoutRequests.value + 1
            intent.removeExtra(WorkoutTrackingService.EXTRA_RESUME_WORKOUT)
        }
    }

    private val ambientLifecycleCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            ambientModeState.value = AmbientModeState(
                isAmbient = true,
                burnInProtectionRequired = ambientDetails.burnInProtectionRequired,
                deviceHasLowBitAmbient = ambientDetails.deviceHasLowBitAmbient,
            )
        }

        override fun onExitAmbient() {
            ambientModeState.value = AmbientModeState()
        }

        override fun onUpdateAmbient() {
            ambientModeState.value = ambientModeState.value.copy(
                ambientUpdateCount = ambientModeState.value.ambientUpdateCount + 1,
            )
        }
    }
}

private fun String?.toLocaleListCompat(): LocaleListCompat {
    return if (this.isNullOrBlank()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(this)
    }
}
