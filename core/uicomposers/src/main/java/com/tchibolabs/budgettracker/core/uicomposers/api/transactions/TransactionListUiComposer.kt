package com.tchibolabs.budgettracker.core.uicomposers.api.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.components.TransactionCard
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme

@Composable
fun TransactionListUiComposer(
    title: String,
    rows: List<TransactionRow>,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No transactions found",
    onRowClick: ((Long) -> Unit)? = null,
    onRowEdit: ((Long) -> Unit)? = null,
    onRowDelete: ((Long) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (rows.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false,
        ) {
            items(rows, key = { it.id }) { row ->
                TransactionCard(
                    category = row.category,
                    note = row.note,
                    dateLabel = row.dateLabel,
                    amountText = row.amountText,
                    currency = row.currency,
                    onClick = { onRowClick?.invoke(row.id) },
                    onEdit = onRowEdit?.let { { it(row.id) } },
                    onDelete = onRowDelete?.let { { it(row.id) } },
                )
            }
        }
    }
}

@Preview
@Composable
private fun TransactionListUiComposerPreview() {
    BudgetTrackerTheme {
        TransactionListUiComposer(
            title = "Top Transactions",
            rows = listOf(
                TransactionRow(1, "Rent", null, "1 Mar 2026", "3442.0", "RON", false),
                TransactionRow(2, "Salary", null, "15 Mar 2026", "1000.0", "EUR", true),
            ),
        )
    }
}
