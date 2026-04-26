package com.tchibolabs.budgettracker.core.data.api.repository

import kotlinx.coroutines.flow.Flow

data class ExchangeRateSnapshot(
    val baseCurrency: String,
    val rates: Map<String, Double>,
    val fetchedAtEpochMs: Long,
)

interface ExchangeRateRepository {
    fun observeLatest(): Flow<ExchangeRateSnapshot?>
    suspend fun refresh(baseCurrency: String): Result<ExchangeRateSnapshot>
    suspend fun convert(amount: Double, from: String, to: String): Double?
}
