package com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform

import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

data class TransactionsFormUiModel(
    val id: Long?,
    val kind: TransactionKind,
    val amountText: String,
    val currency: String,
    val category: String,
    val note: String,
    val isSaving: Boolean,
    val saved: Boolean,
) : UiModel {
    val isValid: Boolean
        get() = amountText.toDoubleOrNull()?.let { it > 0.0 } == true && category.isNotBlank()

    companion object {
        val Initial = TransactionsFormUiModel(
            id = null,
            kind = TransactionKind.Expense,
            amountText = "",
            currency = "USD",
            category = "",
            note = "",
            isSaving = false,
            saved = false,
        )
    }
}

sealed interface TransactionsFormEvent {
    data class KindChanged(val kind: TransactionKind) : TransactionsFormEvent
    data class AmountChanged(val text: String) : TransactionsFormEvent
    data class CurrencyChanged(val currency: String) : TransactionsFormEvent
    data class CategoryChanged(val category: String) : TransactionsFormEvent
    data class NoteChanged(val note: String) : TransactionsFormEvent
    data object Save : TransactionsFormEvent
}
