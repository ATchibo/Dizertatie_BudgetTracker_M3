package com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform

import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

enum class TransactionCategory(val isIncome: Boolean) {
    GROCERIES(false),
    RENT(false),
    BILLS(false),
    TRANSPORTATION(false),
    SUBSCRIPTIONS(false),
    ENTERTAINMENT(false),
    HEALTHCARE(false),
    EMERGENCIES(false),
    SALARY(true),
    REVENUE(true),
    OTHER(false);

    val label: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

data class TransactionsFormUiModel(
    val id: Long?,
    val amountText: String,
    val currency: Currency,
    val category: TransactionCategory,
    val occurredAtEpochMs: Long,
    val description: String,
    val isCurrencyPickerOpen: Boolean,
    val isCategoryPickerOpen: Boolean,
    val isDatePickerOpen: Boolean,
    val isSaving: Boolean,
    val saved: Boolean,
) : UiModel {
    val isValid: Boolean
        get() = amountText.toDoubleOrNull()?.let { it > 0.0 } == true

    companion object {
        val Initial = TransactionsFormUiModel(
            id = null,
            amountText = "",
            currency = Currency.USD,
            category = TransactionCategory.EMERGENCIES,
            occurredAtEpochMs = System.currentTimeMillis(),
            description = "",
            isCurrencyPickerOpen = false,
            isCategoryPickerOpen = false,
            isDatePickerOpen = false,
            isSaving = false,
            saved = false,
        )
    }
}

sealed interface TransactionsFormEvent {
    data class AmountChanged(val text: String) : TransactionsFormEvent
    data class CurrencySelected(val currency: Currency) : TransactionsFormEvent
    data class CategorySelected(val category: TransactionCategory) : TransactionsFormEvent
    data class DateSelected(val epochMs: Long) : TransactionsFormEvent
    data class DescriptionChanged(val text: String) : TransactionsFormEvent
    data object OpenCurrencyPicker : TransactionsFormEvent
    data object DismissCurrencyPicker : TransactionsFormEvent
    data object OpenCategoryPicker : TransactionsFormEvent
    data object DismissCategoryPicker : TransactionsFormEvent
    data object OpenDatePicker : TransactionsFormEvent
    data object DismissDatePicker : TransactionsFormEvent
    data object Save : TransactionsFormEvent
}
