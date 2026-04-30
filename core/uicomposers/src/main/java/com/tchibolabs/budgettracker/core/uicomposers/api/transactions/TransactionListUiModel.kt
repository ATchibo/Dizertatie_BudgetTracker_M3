package com.tchibolabs.budgettracker.core.uicomposers.api.transactions

data class TransactionRow(
    val id: Long,
    val category: String,
    val note: String?,
    val dateLabel: String,
    val amountText: String,
    val currency: String,
    val isIncome: Boolean,
)

enum class TransactionListScope {
    TRANSACTIONS,
    DASHBOARD,
}

data class TransactionListSourceRow(
    val id: Long,
    val category: String,
    val note: String?,
    val dateLabel: String,
    val amount: Double,
    val currency: String,
    val isIncome: Boolean,
)

interface TransactionListUiAdapter {
    fun composeRows(rows: List<TransactionListSourceRow>): List<TransactionRow>
}

interface TransactionListUiAdapterFactory {
    fun create(scope: TransactionListScope): TransactionListUiAdapter
}
