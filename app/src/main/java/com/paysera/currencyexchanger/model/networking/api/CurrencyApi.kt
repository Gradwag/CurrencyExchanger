package com.paysera.currencyexchanger.model.networking.api

import com.paysera.currencyexchanger.model.networking.model.ExchangeRatesResponse
import retrofit2.Call
import retrofit2.http.POST

interface CurrencyApi {
    @POST("/tasks/api/currency-exchange-rates")
    fun getExchangeRates(): Call<ExchangeRatesResponse>
}