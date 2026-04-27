package com.tchibolabs.budgettracker.feature.dashboard.api

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardUiComposer
import com.tchibolabs.budgettracker.feature.dashboard.impl.DashboardUiAdapter

@Composable
fun DashboardEntryPoint(
    modifier: Modifier = Modifier,
    onNavigate: (BudgetRoute) -> Unit,
    adapter: DashboardUiAdapter = hiltViewModel(),
) {
    val uiModel by adapter.uiModel.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(adapter) {
        adapter.conversionError.collect {
            Toast.makeText(
                context,
                "Currency conversion failed. Switched back to 'Selected only'.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    DashboardUiComposer(
        uiModel = uiModel,
        modifier = modifier,
        onEvent = { adapter.onEvent(it) },
        onNavigate = onNavigate,
    )
}
