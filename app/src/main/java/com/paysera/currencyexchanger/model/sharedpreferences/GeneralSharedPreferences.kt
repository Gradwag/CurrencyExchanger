package com.paysera.currencyexchanger.model.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Singleton

@Singleton
class GeneralSharedPreferences(context: Context) {
    private val SHARED_REFS_NAME = "SP_GENERAL"
    private val preferences: SharedPreferences = context.getSharedPreferences(SHARED_REFS_NAME, Context.MODE_PRIVATE)

    var isFirstStart: Boolean
        get() = preferences.getBoolean("isFirstStart", true)
        set(value) = preferences.edit().putBoolean("isFirstStart", value).apply()

    var transactionCount: Int
        get() = preferences.getInt("transactionCount", 0)
        set(value) = preferences.edit().putInt("transactionCount", value).apply()
}