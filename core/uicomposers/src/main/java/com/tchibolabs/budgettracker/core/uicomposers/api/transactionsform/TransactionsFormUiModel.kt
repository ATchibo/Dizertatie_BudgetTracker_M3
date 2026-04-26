package com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform

import androidx.compose.ui.graphics.Color
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

enum class TransactionCategory(val isIncome: Boolean, val color: Color) {
    GROCERIES(false, Color(0xFF4CAF50)),
    RENT(false, Color(0xFF2196F3)),
    BILLS(false, Color(0xFFF44336)),
    TRANSPORTATION(false, Color(0xFFFF9800)),
    SUBSCRIPTIONS(false, Color(0xFF9C27B0)),
    ENTERTAINMENT(false, Color(0xFFE91E63)),
    HEALTHCARE(false, Color(0xFF00BCD4)),
    EMERGENCIES(false, Color(0xFF795548)),
    SALARY(true, Color(0xFF8BC34A)),
    REVENUE(true, Color(0xFFCDDC39)),
    OTHER(false, Color(0xFF9E9E9E));

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
