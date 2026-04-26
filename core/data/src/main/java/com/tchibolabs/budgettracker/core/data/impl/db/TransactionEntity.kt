package com.tchibolabs.budgettracker.core.data.impl.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val note: String?,
    val occurredAtEpochMs: Long,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    kind = TransactionKind.valueOf(kind),
    amount = amount,
    currency = currency,
    category = category,
    note = note,
    occurredAtEpochMs = occurredAtEpochMs,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    kind = kind.name,
    amount = amount,
    currency = currency,
    category = category,
    note = note,
    occurredAtEpochMs = occurredAtEpochMs,
)
