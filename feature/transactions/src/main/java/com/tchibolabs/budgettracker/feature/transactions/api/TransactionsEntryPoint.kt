package com.tchibolabs.budgettracker.feature.transactions.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactions.TransactionsUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactions.TransactionsUiComposer

@Composable
fun TransactionsEntryPoint(
    modifier: Modifier = Modifier,
    onNavigate: (BudgetRoute) -> Unit,
    adapter: TransactionsUiAdapter = hiltViewModel(),
) {
    val uiModel by adapter.uiModel.collectAsStateWithLifecycle()
    TransactionsUiComposer(
        uiModel = uiModel,
        modifier = modifier,
        onEvent = { adapter.onEvent(it) },
        onNavigate = onNavigate,
    )
}
