package com.tchibolabs.budgettracker.feature.transactions.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.components.FilterChipCard
import com.tchibolabs.budgettracker.core.design.api.components.TransactionCard
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute

@Composable
internal fun TransactionsUiComposer(
    uiModel: TransactionsUiModel,
    modifier: Modifier = Modifier,
    onEvent: (TransactionsEvent) -> Unit,
    onNavigate: (BudgetRoute) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChipCard(
                        label = "Time Period",
                        value = uiModel.period.label,
                        onClick = { onEvent(TransactionsEvent.CyclePeriod) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChipCard(
                        label = "Order",
                        value = uiModel.order.label,
                        onClick = { onEvent(TransactionsEvent.CycleOrder) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            items(uiModel.rows, key = { it.id }) { row ->
                TransactionCard(
                    category = row.category,
                    note = row.note,
                    dateLabel = row.dateLabel,
                    amountText = row.amountText,
                    currency = row.currency,
                    onClick = { onNavigate(BudgetRoute.TransactionsForm(row.id)) },
                    onEdit = { onNavigate(BudgetRoute.TransactionsForm(row.id)) },
                    onDelete = { onEvent(TransactionsEvent.Delete(row.id)) },
                )
            }
        }
        FloatingActionButton(
            onClick = { onNavigate(BudgetRoute.TransactionsForm()) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add transaction")
        }
    }
}

@Preview
@Composable
private fun TransactionsUiComposerPreview() {
    BudgetTrackerTheme {
        TransactionsUiComposer(
            uiModel = TransactionsUiModel(
                rows = listOf(
                    TransactionRow(1, "Entertainment", null, "22 Mar 2026", "3442.0", "RON", false),
                    TransactionRow(2, "Healthcare", "dentist visit", "22 Mar 2026", "457.0", "EUR", false),
                    TransactionRow(3, "Transportation", null, "22 Mar 2026", "456.0", "RON", false),
                    TransactionRow(4, "Salary", null, "22 Mar 2026", "257.0", "RON", true),
                ),
                period = TimePeriod.Past31Days,
                order = SortOrder.AmountDescending,
                isLoading = false,
            ),
            onEvent = {},
            onNavigate = {},
        )
    }
}
