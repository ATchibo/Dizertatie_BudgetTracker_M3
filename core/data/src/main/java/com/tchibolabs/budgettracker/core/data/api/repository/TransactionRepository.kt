package com.tchibolabs.budgettracker.core.data.api.repository

import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    suspend fun findById(id: Long): Transaction?
    suspend fun upsert(transaction: Transaction): Long
    suspend fun delete(id: Long)
}
