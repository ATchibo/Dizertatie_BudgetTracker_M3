package com.tchibolabs.budgettracker.core.data.api.model

enum class TransactionKind { Income, Expense }

enum class Currency {
    USD,
    EUR,
    RON,
}

enum class TransactionOrder {
    DATE_ASC,
    DATE_DESC,
    AMOUNT_ASC,
    AMOUNT_DESC,
}

enum class TransactionPeriod {
    TODAY,
    PAST_7_DAYS,
    PAST_31_DAYS,
    PAST_YEAR,
    CURRENT_MONTH,
    ALL_TIME,
}

data class Transaction(
    val id: Long,
    val kind: TransactionKind,
    val amount: Double,
    val currency: Currency,
    val category: String,
    val note: String?,
    val occurredAtEpochMs: Long,
)
