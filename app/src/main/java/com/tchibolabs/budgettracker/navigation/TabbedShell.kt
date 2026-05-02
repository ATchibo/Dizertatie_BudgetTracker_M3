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
    val items = remember {
        listOf(
            BottomBarItem(
                key = BudgetTab.Transactions.routeKey,
                label = "Transactions",
                icon = Icons.AutoMirrored.Filled.List,
            ),
            BottomBarItem(
                key = BudgetTab.Dashboard.routeKey,
                label = "Dashboard",
                icon = Icons.Filled.GridView,
            ),
        )
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { BudgetTopAppBar(title = "BudgetTracker") },
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
