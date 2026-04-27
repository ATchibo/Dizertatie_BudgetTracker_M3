package com.tchibolabs.budgettracker.core.data.impl.repository

import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateRepository
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateSnapshot
import com.tchibolabs.budgettracker.core.remote.api.ExchangeRatesApi
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: ExchangeRatesApi,
) : ExchangeRateRepository {

    private val cache = ConcurrentHashMap<String, Map<String, Double>>()
    private val _latest = MutableStateFlow<ExchangeRateSnapshot?>(null)

    override fun observeLatest(): Flow<ExchangeRateSnapshot?> = _latest.asStateFlow()

    override suspend fun refresh(baseCurrency: String): Result<ExchangeRateSnapshot> {
        return try {
            val targets = Currency.values()
                .filter { it.name != baseCurrency }
                .joinToString(",") { it.name }
            val response = api.getLatest(baseCurrency, targets)
            val snapshot = ExchangeRateSnapshot(
                baseCurrency = response.base,
                rates = response.rates,
                fetchedAtEpochMs = System.currentTimeMillis(),
            )
            cache[snapshot.baseCurrency] = snapshot.rates
            _latest.value = snapshot
            Result.success(snapshot)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun convert(amount: Double, from: String, to: String): Double? {
        if (from == to) return amount
        val rate = cache[from]?.get(to) ?: return null
        return amount * rate
    }
}
