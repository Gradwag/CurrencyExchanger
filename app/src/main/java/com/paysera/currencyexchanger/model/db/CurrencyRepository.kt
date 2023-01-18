package com.paysera.currencyexchanger.model.db

import androidx.annotation.WorkerThread
import com.paysera.currencyexchanger.model.db.dao.CurrencyDao
import com.paysera.currencyexchanger.model.db.entity.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

class CurrencyRepository(private val currencyDao: CurrencyDao) {
    @WorkerThread
    suspend fun insertCurrencies(currencies: List<Currency>) = withContext(Dispatchers.IO) {
        currencyDao.insertCurrencies(currencies)
    }

    @WorkerThread
    suspend fun getCurrency(currency: String): Currency = withContext(Dispatchers.IO) {
        currencyDao.getCurrency(currency)
    }

    @WorkerThread
    suspend fun getAllCurrencies(): List<Currency> = withContext(Dispatchers.IO) {
        currencyDao.getAllCurrencies()
    }
}