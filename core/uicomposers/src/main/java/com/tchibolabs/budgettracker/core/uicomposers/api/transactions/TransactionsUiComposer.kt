package com.tchibolabs.budgettracker.core.uicomposers.api.transactions

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.data.api.model.TransactionOrder
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.design.api.components.FilterChipCard
import com.tchibolabs.budgettracker.core.design.api.components.OptionsBottomSheet
import com.tchibolabs.budgettracker.core.design.api.components.PickerOption
import com.tchibolabs.budgettracker.core.design.api.components.TransactionCard
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute

@Composable
fun TransactionsUiComposer(
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
                    uiModel.filters.forEach { filter ->
                        FilterChipCard(
                            label = filter.label,
                            value = filter.selectedLabel,
                            onClick = { onEvent(TransactionsEvent.OpenPicker(filter.id)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
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
                    onDelete = { onEvent(TransactionsEvent.RequestDelete(row.id)) },
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

        uiModel.filters.firstOrNull { it.isPickerOpen }?.let { filter ->
            OptionsBottomSheet(
                title = filter.label,
                options = filter.options.map { PickerOption(it.id, it.label) },
                selectedOptionId = filter.selectedOptionId,
                onSelect = { option ->
                    onEvent(TransactionsEvent.SelectOption(filter.id, option.id))
                },
                onDismiss = { onEvent(TransactionsEvent.ClosePicker(filter.id)) },
            )
        }

        if (uiModel.pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { onEvent(TransactionsEvent.CancelDelete) },
                title = { Text("Delete transaction?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { onEvent(TransactionsEvent.ConfirmDelete) }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TransactionsEvent.CancelDelete) }) {
                        Text("Cancel")
                    }
                },
            )
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
                ),
                filters = listOf(
                    TransactionsFilter(
                        id = TransactionsFilter.ID_PERIOD,
                        label = "Time Period",
                        options = TransactionPeriod.values().map { FilterOption(it.name, it.label) },
                        selectedOptionId = TransactionPeriod.PAST_31_DAYS.name,
                        isPickerOpen = false,
                    ),
                    TransactionsFilter(
                        id = TransactionsFilter.ID_ORDER,
                        label = "Order",
                        options = TransactionOrder.values().map { FilterOption(it.name, it.label) },
                        selectedOptionId = TransactionOrder.AMOUNT_DESC.name,
                        isPickerOpen = false,
                    ),
                ),
                pendingDeleteId = null,
                isLoading = false,
            ),
            onEvent = {},
            onNavigate = {},
        )
    }
}
