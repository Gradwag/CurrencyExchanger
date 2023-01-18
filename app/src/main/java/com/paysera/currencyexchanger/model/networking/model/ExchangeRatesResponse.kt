package com.paysera.currencyexchanger.model.networking.model

import com.google.gson.annotations.SerializedName

data class ExchangeRatesResponse(
    @SerializedName("base") val base: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("rates") val rates: Map<String, Double>
)