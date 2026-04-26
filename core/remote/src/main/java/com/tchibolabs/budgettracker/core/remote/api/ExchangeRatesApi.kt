package com.tchibolabs.budgettracker.core.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class ExchangeRatesResponse(
    @SerialName("base_code") val base: String,
    @SerialName("rates") val rates: Map<String, Double>,
)

interface ExchangeRatesApi {
    /**
     * Backed by https://open.er-api.com — free, no API key required.
     * Path-style: /v6/latest/{base}.
     */
    @GET("v6/latest/{base}")
    suspend fun getLatest(@Path("base") base: String): ExchangeRatesResponse
}
