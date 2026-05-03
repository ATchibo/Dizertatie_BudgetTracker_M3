package com.tchibolabs.budgettracker.feature.transactionsform.api

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactionsform.TransactionsFormUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactionsform.TransactionsFormUiComposer
import com.tchibolabs.budgettracker.feature.transactionsform.R

@Composable
fun TransactionsFormEntryPoint(
    modifier: Modifier = Modifier,
    transactionId: Long?,
    onNavigate: (BudgetRoute) -> Unit,
    adapter: TransactionsFormUiAdapter = hiltViewModel(),
) {
    val context = LocalContext.current
    LaunchedEffect(transactionId) { adapter.load(transactionId) }
    LaunchedEffect(adapter) {
        adapter.saveError.collect {
            Toast.makeText(context, context.getString(R.string.form_save_error), Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(adapter) {
        adapter.saved.collect { onNavigate(BudgetRoute.Transactions) }
    }
    val uiModel by adapter.uiModel.collectAsStateWithLifecycle()
    TransactionsFormUiComposer(
        uiModel = uiModel,
        modifier = modifier,
        onEvent = { adapter.onEvent(it) },
        onCancel = { onNavigate(BudgetRoute.Transactions) },
    )
}
