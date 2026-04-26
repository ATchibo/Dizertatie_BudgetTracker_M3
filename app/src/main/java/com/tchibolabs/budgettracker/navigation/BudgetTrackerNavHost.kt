package com.tchibolabs.budgettracker.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.navigation.api.BudgetTab
import com.tchibolabs.budgettracker.feature.transactionsform.api.TransactionsFormEntryPoint

private const val NO_TX_ID = Long.MIN_VALUE

@Composable
fun BudgetTrackerNavHost(contentPadding: PaddingValues) {
    var isFormOpen by rememberSaveable { mutableStateOf(false) }
    var formTransactionId by rememberSaveable { mutableStateOf(NO_TX_ID) }
    var tab by rememberSaveable { mutableStateOf(BudgetTab.Transactions) }

    val rootModifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)

    val closeForm = {
        isFormOpen = false
        formTransactionId = NO_TX_ID
        tab = BudgetTab.Transactions
    }

    if (isFormOpen) {
        BackHandler(onBack = closeForm)
        TransactionsFormEntryPoint(
            modifier = rootModifier,
            transactionId = if (formTransactionId == NO_TX_ID) null else formTransactionId,
            onNavigate = { target ->
                when (target) {
                    is BudgetRoute.TransactionsForm -> {
                        formTransactionId = target.transactionId ?: NO_TX_ID
                    }
                    BudgetRoute.Dashboard -> {
                        tab = BudgetTab.Dashboard
                        isFormOpen = false
                        formTransactionId = NO_TX_ID
                    }
                    BudgetRoute.Transactions, BudgetRoute.Home -> {
                        tab = BudgetTab.Transactions
                        isFormOpen = false
                        formTransactionId = NO_TX_ID
                    }
                }
            },
        )
    } else {
        TabbedShell(
            modifier = rootModifier,
            selectedTab = tab,
            onSelectTab = { tab = it },
            onNavigate = { target ->
                when (target) {
                    is BudgetRoute.TransactionsForm -> {
                        formTransactionId = target.transactionId ?: NO_TX_ID
                        isFormOpen = true
                    }
                    BudgetRoute.Dashboard -> tab = BudgetTab.Dashboard
                    BudgetRoute.Transactions -> tab = BudgetTab.Transactions
                    BudgetRoute.Home -> Unit
                }
            },
        )
    }
}
