package com.tchibolabs.budgettracker.core.data.impl.repository

import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateRepository
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateSnapshot
import com.tchibolabs.budgettracker.core.data.impl.db.ExchangeRateDao
import com.tchibolabs.budgettracker.core.data.impl.db.ExchangeRateEntity
import com.tchibolabs.budgettracker.core.remote.api.ExchangeRatesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

internal class ExchangeRateRepositoryImpl @Inject constructor(
    private val dao: ExchangeRateDao,
    private val api: ExchangeRatesApi,
    private val json: Json,
) : ExchangeRateRepository {

    private val mapSerializer = MapSerializer(String.serializer(), Double.serializer())

    override fun observeLatest(): Flow<ExchangeRateSnapshot?> =
        dao.observeLatest().map { entity -> entity?.toSnapshot() }

    override suspend fun refresh(baseCurrency: String): Result<ExchangeRateSnapshot> = runCatching {
        val response = api.getLatest(baseCurrency)
        val snapshot = ExchangeRateSnapshot(
            baseCurrency = response.base,
            rates = response.rates,
            fetchedAtEpochMs = System.currentTimeMillis(),
        )
        dao.upsert(
            ExchangeRateEntity(
                baseCurrency = snapshot.baseCurrency,
                ratesJson = json.encodeToString(mapSerializer, snapshot.rates),
                fetchedAtEpochMs = snapshot.fetchedAtEpochMs,
            ),
        )
        snapshot
    }

    override suspend fun convert(amount: Double, from: String, to: String): Double? {
        if (from == to) return amount
        val cached = dao.findByBase(from) ?: return null
        val rates = json.decodeFromString(mapSerializer, cached.ratesJson)
        val rate = rates[to] ?: return null
        return amount * rate
    }

    private fun ExchangeRateEntity.toSnapshot(): ExchangeRateSnapshot =
        ExchangeRateSnapshot(
            baseCurrency = baseCurrency,
            rates = json.decodeFromString(mapSerializer, ratesJson),
            fetchedAtEpochMs = fetchedAtEpochMs,
        )
}
