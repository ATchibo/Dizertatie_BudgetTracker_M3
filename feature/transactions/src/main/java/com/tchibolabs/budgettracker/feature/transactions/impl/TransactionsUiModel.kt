package com.tchibolabs.budgettracker.feature.transactions.impl

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

data class TransactionsUiModel(
    val rows: List<TransactionRow>,
    val period: TimePeriod,
    val order: SortOrder,
    val isLoading: Boolean,
) : UiModel {
    companion object {
        val Initial = TransactionsUiModel(
            rows = emptyList(),
            period = TimePeriod.Past31Days,
            order = SortOrder.AmountDescending,
            isLoading = true,
        )
    }
}
