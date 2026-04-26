package com.tchibolabs.budgettracker.core.data.impl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates ORDER BY fetchedAtEpochMs DESC LIMIT 1")
    fun observeLatest(): Flow<ExchangeRateEntity?>

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base LIMIT 1")
    suspend fun findByBase(base: String): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExchangeRateEntity)
}
