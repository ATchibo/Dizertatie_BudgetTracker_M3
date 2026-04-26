package com.tchibolabs.budgettracker.feature.dashboard.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    DashboardUiComposer(
        uiModel = uiModel,
        modifier = modifier,
        onIntervalSelected = { adapter.onEvent(it) },
        onNavigate = onNavigate,
    )
}
