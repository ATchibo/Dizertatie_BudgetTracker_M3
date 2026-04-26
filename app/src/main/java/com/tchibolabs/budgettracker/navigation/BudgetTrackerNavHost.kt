package com.tchibolabs.budgettracker.navigation

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

@Composable
fun BudgetTrackerNavHost(contentPadding: PaddingValues) {
    var route by rememberSaveable { mutableStateOf<BudgetRoute>(BudgetRoute.Transactions) }
    var tab by rememberSaveable { mutableStateOf(BudgetTab.Transactions) }

    val rootModifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)

    when (val current = route) {
        is BudgetRoute.TransactionsForm -> TransactionsFormEntryPoint(
            modifier = rootModifier,
            transactionId = current.transactionId,
            onNavigate = { target ->
                route = if (target is BudgetRoute.TransactionsForm) target else BudgetRoute.Transactions
                if (target is BudgetRoute.Dashboard) tab = BudgetTab.Dashboard
                if (target is BudgetRoute.Transactions) tab = BudgetTab.Transactions
            },
        )
        else -> TabbedShell(
            modifier = rootModifier,
            selectedTab = tab,
            onSelectTab = { tab = it },
            onNavigate = { target ->
                when (target) {
                    is BudgetRoute.TransactionsForm -> route = target
                    BudgetRoute.Dashboard -> tab = BudgetTab.Dashboard
                    BudgetRoute.Transactions -> tab = BudgetTab.Transactions
                    BudgetRoute.Home -> Unit
                }
            },
        )
    }
}
