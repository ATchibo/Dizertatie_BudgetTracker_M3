package com.tchibolabs.budgettracker.core.data.impl.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, ExchangeRateEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun exchangeRateDao(): ExchangeRateDao
}
