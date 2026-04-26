package com.tchibolabs.budgettracker.feature.transactionsform.impl

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
