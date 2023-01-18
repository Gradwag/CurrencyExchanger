package com.paysera.currencyexchanger.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paysera.currencyexchanger.model.db.entity.Balance

@Dao
interface BalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(currency: Balance)

    @Query("SELECT * FROM Balance ORDER BY currency ASC")
    suspend fun getAllBalances(): List<Balance>

    @Query("SELECT * FROM Balance WHERE currency = :currency")
    suspend fun getBalance(currency: String): Balance
}