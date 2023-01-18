package com.paysera.currencyexchanger.model.db

import androidx.annotation.WorkerThread
import com.paysera.currencyexchanger.model.db.dao.BalanceDao
import com.paysera.currencyexchanger.model.db.entity.Balance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BalanceRepository(private val balanceDao: BalanceDao) {
    @WorkerThread
    suspend fun insertBalance(currency: Balance) = withContext(Dispatchers.IO) {
        balanceDao.insertBalance(currency)
    }

    @WorkerThread
    suspend fun getAllBalances(): List<Balance> = withContext(Dispatchers.IO) {
        balanceDao.getAllBalances()
    }

    @WorkerThread
    suspend fun getBalance(currency: String): Balance = withContext(Dispatchers.IO) {
        balanceDao.getBalance(currency)
    }
}