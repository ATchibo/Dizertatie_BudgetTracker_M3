package com.tchibolabs.budgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.navigation.BudgetTrackerNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    BudgetTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BudgetTrackerNavHost(contentPadding = PaddingValues())
        }
    }
}
