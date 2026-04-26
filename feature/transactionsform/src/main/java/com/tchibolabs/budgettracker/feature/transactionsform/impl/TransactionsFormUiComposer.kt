package com.tchibolabs.budgettracker.feature.transactionsform.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.design.api.components.BudgetTopAppBar
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme

@Composable
internal fun TransactionsFormUiComposer(
    uiModel: TransactionsFormUiModel,
    modifier: Modifier = Modifier,
    onEvent: (TransactionsFormEvent) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    LaunchedEffect(uiModel.saved) {
        if (uiModel.saved) onSaved()
    }
    Column(modifier = modifier.fillMaxSize()) {
        BudgetTopAppBar(
            title = if (uiModel.id == null) "Add transaction" else "Edit transaction",
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionKind.values().forEach { kind ->
                    FilterChip(
                        selected = kind == uiModel.kind,
                        onClick = { onEvent(TransactionsFormEvent.KindChanged(kind)) },
                        label = { Text(kind.name) },
                    )
                }
            }
            OutlinedTextField(
                value = uiModel.amountText,
                onValueChange = { onEvent(TransactionsFormEvent.AmountChanged(it)) },
                label = { Text("Amount") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiModel.currency,
                onValueChange = { onEvent(TransactionsFormEvent.CurrencyChanged(it.uppercase())) },
                label = { Text("Currency (ISO)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiModel.category,
                onValueChange = { onEvent(TransactionsFormEvent.CategoryChanged(it)) },
                label = { Text("Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiModel.note,
                onValueChange = { onEvent(TransactionsFormEvent.NoteChanged(it)) },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
                Button(
                    onClick = { onEvent(TransactionsFormEvent.Save) },
                    enabled = uiModel.isValid && !uiModel.isSaving,
                ) {
                    Text(if (uiModel.isSaving) "Saving…" else "Save")
                }
            }
        }
    }
}

@Preview
@Composable
private fun TransactionsFormUiComposerPreview() {
    BudgetTrackerTheme {
        TransactionsFormUiComposer(
            uiModel = TransactionsFormUiModel.Initial.copy(amountText = "12.50", category = "Food"),
            onEvent = {},
            onSaved = {},
            onCancel = {},
        )
    }
}
