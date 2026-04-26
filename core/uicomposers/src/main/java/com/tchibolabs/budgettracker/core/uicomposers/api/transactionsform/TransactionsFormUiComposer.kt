package com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.design.api.components.BudgetTopAppBar
import com.tchibolabs.budgettracker.core.design.api.components.FilterChipCard
import com.tchibolabs.budgettracker.core.design.api.components.OptionsBottomSheet
import com.tchibolabs.budgettracker.core.design.api.components.PickerOption
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsFormUiComposer(
    uiModel: TransactionsFormUiModel,
    modifier: Modifier = Modifier,
    onEvent: (TransactionsFormEvent) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    LaunchedEffect(uiModel.saved) {
        if (uiModel.saved) onSaved()
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BudgetTopAppBar(
                title = if (uiModel.id == null) "Add Transaction" else "Edit Transaction",
                onBack = onCancel,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AmountField(
                value = uiModel.amountText,
                onValueChange = { onEvent(TransactionsFormEvent.AmountChanged(it)) },
            )
            FilterChipCard(
                label = "Currency",
                value = uiModel.currency.name,
                onClick = { onEvent(TransactionsFormEvent.OpenCurrencyPicker) },
            )
            FilterChipCard(
                label = "Category",
                value = uiModel.category.label.uppercase(),
                onClick = { onEvent(TransactionsFormEvent.OpenCategoryPicker) },
            )
            FilterChipCard(
                label = "Date",
                value = uiModel.occurredAtEpochMs.formatDate(),
                onClick = { onEvent(TransactionsFormEvent.OpenDatePicker) },
            )
            DescriptionField(
                value = uiModel.description,
                onValueChange = { onEvent(TransactionsFormEvent.DescriptionChanged(it)) },
            )
            Box(modifier = Modifier.padding(top = 8.dp)) {
                Button(
                    onClick = { onEvent(TransactionsFormEvent.Save) },
                    enabled = uiModel.isValid && !uiModel.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = if (uiModel.isSaving) "Saving…" else "Save",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }

    if (uiModel.isCurrencyPickerOpen) {
        OptionsBottomSheet(
            title = "Currency",
            options = Currency.values().map { PickerOption(it.name, it.name) },
            selectedOptionId = uiModel.currency.name,
            onSelect = { option ->
                Currency.values().firstOrNull { it.name == option.id }?.let {
                    onEvent(TransactionsFormEvent.CurrencySelected(it))
                }
            },
            onDismiss = { onEvent(TransactionsFormEvent.DismissCurrencyPicker) },
        )
    }

    if (uiModel.isCategoryPickerOpen) {
        OptionsBottomSheet(
            title = "Category",
            options = TransactionCategory.values().map { PickerOption(it.name, it.label) },
            selectedOptionId = uiModel.category.name,
            onSelect = { option ->
                TransactionCategory.values().firstOrNull { it.name == option.id }?.let {
                    onEvent(TransactionsFormEvent.CategorySelected(it))
                }
            },
            onDismiss = { onEvent(TransactionsFormEvent.DismissCategoryPicker) },
        )
    }

    if (uiModel.isDatePickerOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiModel.occurredAtEpochMs,
        )
        DatePickerDialog(
            onDismissRequest = { onEvent(TransactionsFormEvent.DismissDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    val picked = datePickerState.selectedDateMillis
                        ?: uiModel.occurredAtEpochMs
                    onEvent(TransactionsFormEvent.DateSelected(picked))
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(TransactionsFormEvent.DismissDatePicker) }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                buildAmountLabel(),
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    )
}

@Composable
private fun buildAmountLabel(): androidx.compose.ui.text.AnnotatedString {
    val color = MaterialTheme.colorScheme.error
    return androidx.compose.ui.text.buildAnnotatedString {
        append("Amount ")
        pushStyle(androidx.compose.ui.text.SpanStyle(color = color))
        append("*")
        pop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    )
}

private fun Long.formatDate(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(dateFormatter)

@Preview
@Composable
private fun TransactionsFormUiComposerPreview() {
    BudgetTrackerTheme {
        TransactionsFormUiComposer(
            uiModel = TransactionsFormUiModel.Initial.copy(
                amountText = "1000",
                description = "Fix water pipes",
            ),
            onEvent = {},
            onSaved = {},
            onCancel = {},
        )
    }
}
