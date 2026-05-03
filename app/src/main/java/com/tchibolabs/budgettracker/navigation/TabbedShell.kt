package com.tchibolabs.budgettracker.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tchibolabs.budgettracker.R
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.design.api.components.BottomBarItem
import com.tchibolabs.budgettracker.core.design.api.components.BudgetBottomBar
import com.tchibolabs.budgettracker.core.design.api.components.BudgetTopAppBar
import com.tchibolabs.budgettracker.core.navigation.api.BudgetTab

@Composable
fun TabbedShell(
    selectedTab: BudgetTab,
    onSelectTab: (BudgetTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val transactionsLabel = stringResource(R.string.tab_transactions)
    val dashboardLabel = stringResource(R.string.tab_dashboard)
    val appName = stringResource(R.string.app_name)
    val items = remember(transactionsLabel, dashboardLabel) {
        listOf(
            BottomBarItem(
                key = BudgetTab.Transactions.routeKey,
                label = transactionsLabel,
                icon = Icons.AutoMirrored.Filled.List,
            ),
            BottomBarItem(
                key = BudgetTab.Dashboard.routeKey,
                label = dashboardLabel,
                icon = Icons.Filled.GridView,
            ),
        )
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { BudgetTopAppBar(title = appName) },
        bottomBar = {
            BudgetBottomBar(
                items = items,
                selectedKey = selectedTab.routeKey,
                onSelect = { item ->
                    val tab = BudgetTab.entries.first { it.routeKey == item.key }
                    onSelectTab(tab)
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun TabbedShellPreview() {
    BudgetTrackerTheme {
        TabbedShell(
            selectedTab = BudgetTab.Transactions,
            onSelectTab = {},
        ) {}
    }
}
