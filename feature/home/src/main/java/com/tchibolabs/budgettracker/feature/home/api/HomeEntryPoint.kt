package com.tchibolabs.budgettracker.feature.home.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.feature.home.impl.uicomposers.HomeUiAdapter
import com.tchibolabs.budgettracker.feature.home.impl.uicomposers.HomeUiComposer

@Composable
fun HomeEntryPoint(
    modifier: Modifier = Modifier,
    onNavigate: (BudgetRoute) -> Unit,
    adapter: HomeUiAdapter = hiltViewModel(),
) {
    val uiModel by adapter.uiModel.collectAsStateWithLifecycle()
    HomeUiComposer(
        uiModel = uiModel,
        modifier = modifier,
        onNavigate = onNavigate,
    )
}
