package com.tchibolabs.budgettracker.core.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class ExchangeRatesResponse(
    @SerialName("base") val base: String,
    @SerialName("rates") val rates: Map<String, Double>,
)

interface ExchangeRatesApi {
    @GET("v1/latest")
    suspend fun getLatest(
        @Query("from") base: String,
        @Query("to") targets: String,
    ): ExchangeRatesResponse
}
