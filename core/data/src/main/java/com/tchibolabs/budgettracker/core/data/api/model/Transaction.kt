package com.tchibolabs.budgettracker.core.data.api.model

enum class TransactionKind { Income, Expense }

data class Transaction(
    val id: Long,
    val kind: TransactionKind,
    val amount: Double,
    val currency: String,
    val category: String,
    val note: String?,
    val occurredAtEpochMs: Long,
)
