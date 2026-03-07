package com.fitnessrepcounter.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fitnessrepcounter.wear.navigation.AppNavGraph
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as FitnessRepCounterApplication).appContainer

        setContent {
            FitnessRepCounterTheme {
                AppNavGraph(viewModelFactory = appContainer.viewModelFactory)
            }
        }
    }
}
