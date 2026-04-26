package com.tchibolabs.budgettracker.feature.transactionsform.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormUiComposer
import com.tchibolabs.budgettracker.feature.transactionsform.impl.TransactionsFormUiAdapter

@Composable
fun TransactionsFormEntryPoint(
    modifier: Modifier = Modifier,
    transactionId: Long?,
    onNavigate: (BudgetRoute) -> Unit,
    adapter: TransactionsFormUiAdapter = hiltViewModel(),
) {
    LaunchedEffect(transactionId) { adapter.load(transactionId) }
    val uiModel by adapter.uiModel.collectAsStateWithLifecycle()
    TransactionsFormUiComposer(
        uiModel = uiModel,
        modifier = modifier,
        onEvent = { adapter.onEvent(it) },
        onSaved = { onNavigate(BudgetRoute.Transactions) },
        onCancel = { onNavigate(BudgetRoute.Transactions) },
    )
}
