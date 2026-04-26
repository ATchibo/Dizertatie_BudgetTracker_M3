package com.tchibolabs.budgettracker.core.data.impl.repository

import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.data.impl.db.TransactionDao
import com.tchibolabs.budgettracker.core.data.impl.db.toDomain
import com.tchibolabs.budgettracker.core.data.impl.db.toEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun findById(id: Long): Transaction? =
        dao.findById(id)?.toDomain()

    override suspend fun upsert(transaction: Transaction): Long =
        dao.upsert(transaction.toEntity())

    override suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
