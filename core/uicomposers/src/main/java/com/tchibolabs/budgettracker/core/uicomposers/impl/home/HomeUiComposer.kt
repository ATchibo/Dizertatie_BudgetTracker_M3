package com.tchibolabs.budgettracker.core.uicomposers.impl.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.components.BudgetTopAppBar
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute
import com.tchibolabs.budgettracker.core.uicomposers.api.home.HomeUiModel

@Composable
fun HomeUiComposer(
    uiModel: HomeUiModel,
    modifier: Modifier = Modifier,
    onNavigate: (BudgetRoute) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BudgetTopAppBar(title = "Home")
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Balance: %.2f %s".format(uiModel.totalBalance, uiModel.displayCurrency),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Recent transactions: ${uiModel.recentTransactionCount}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { onNavigate(BudgetRoute.Transactions) }) {
                Text("View transactions")
            }
            Button(onClick = { onNavigate(BudgetRoute.Dashboard) }) {
                Text("Open dashboard")
            }
            Button(onClick = { onNavigate(BudgetRoute.TransactionsForm()) }) {
                Text("Add transaction")
            }
        }
    }
}

@Preview
@Composable
private fun HomeUiComposerPreview() {
    BudgetTrackerTheme {
        HomeUiComposer(
            uiModel = HomeUiModel(1234.56, "USD", 12, false),
            onNavigate = {},
        )
    }
}
