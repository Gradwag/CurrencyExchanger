package com.paysera.currencyexchanger.model.db.dao

import androidx.room.*
import com.paysera.currencyexchanger.model.db.entity.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<Currency>)

    @Update
    suspend fun updateCurrency(comic: Currency)

    @Query("SELECT * FROM Currency ORDER by currency ASC")
    suspend fun getAllCurrencies(): List<Currency>

    @Query("SELECT * FROM Currency WHERE currency = :currency")
    suspend fun getCurrency(currency: String): Currency
}