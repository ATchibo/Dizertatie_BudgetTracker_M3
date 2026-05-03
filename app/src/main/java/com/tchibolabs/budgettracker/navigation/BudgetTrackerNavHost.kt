package com.tchibolabs.budgettracker.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.navigation.api.BudgetTab
import com.tchibolabs.budgettracker.feature.dashboard.api.DashboardEntryPoint
import com.tchibolabs.budgettracker.feature.transactions.api.TransactionsEntryPoint
import com.tchibolabs.budgettracker.feature.transactionsform.api.TransactionsFormEntryPoint

@Composable
fun BudgetTrackerNavHost() {
    val backStack = rememberNavBackStack(BudgetRoute.Transactions)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry(BudgetRoute.Transactions) {
                TabbedShell(
                    selectedTab = BudgetTab.Transactions,
                    onSelectTab = { backStack[backStack.lastIndex] = it.toRoute() },
                ) {
                    TransactionsEntryPoint(
                        onNavigate = { if (it is BudgetRoute.TransactionsForm) backStack.add(it) },
                    )
                }
            }
            entry(BudgetRoute.Dashboard) {
                TabbedShell(
                    selectedTab = BudgetTab.Dashboard,
                    onSelectTab = { backStack[backStack.lastIndex] = it.toRoute() },
                ) {
                    DashboardEntryPoint()
                }
            }
            entry<BudgetRoute.TransactionsForm> { route ->
                TransactionsFormEntryPoint(
                    transactionId = route.transactionId,
                    onNavigate = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

private fun BudgetTab.toRoute(): BudgetRoute = when (this) {
    BudgetTab.Transactions -> BudgetRoute.Transactions
    BudgetTab.Dashboard -> BudgetRoute.Dashboard
}
