package com.tchibolabs.budgettracker.core.uicomposers.api.transactions

import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

enum class TimePeriod(val label: String) {
    Past7Days("Past 7 days"),
    Past31Days("Past 31 days"),
    AllTime("All time"),
}

enum class SortOrder(val label: String) {
    AmountDescending("Amount (Descending)"),
    AmountAscending("Amount (Ascending)"),
    DateNewest("Date (Newest first)"),
    DateOldest("Date (Oldest first)"),
}

data class TransactionRow(
    val id: Long,
    val category: String,
    val note: String?,
    val dateLabel: String,
    val amountText: String,
    val currency: String,
    val isIncome: Boolean,
)

data class FilterOption(
    val id: String,
    val label: String,
)

data class TransactionsFilter(
    val id: String,
    val label: String,
    val options: List<FilterOption>,
    val selectedOptionId: String,
    val isPickerOpen: Boolean,
) {
    val selectedLabel: String
        get() = options.firstOrNull { it.id == selectedOptionId }?.label.orEmpty()

    companion object {
        const val ID_PERIOD = "period"
        const val ID_ORDER = "order"
    }
}

data class TransactionsUiModel(
    val rows: List<TransactionRow>,
    val filters: List<TransactionsFilter>,
    val pendingDeleteId: Long?,
    val isLoading: Boolean,
) : UiModel {
    companion object {
        val Initial = TransactionsUiModel(
            rows = emptyList(),
            filters = emptyList(),
            pendingDeleteId = null,
            isLoading = true,
        )
    }
}

sealed interface TransactionsEvent {
    data class OpenPicker(val filterId: String) : TransactionsEvent
    data class ClosePicker(val filterId: String) : TransactionsEvent
    data class SelectOption(val filterId: String, val optionId: String) : TransactionsEvent
    data class RequestDelete(val id: Long) : TransactionsEvent
    data object ConfirmDelete : TransactionsEvent
    data object CancelDelete : TransactionsEvent
}
