package com.paysera.currencyexchanger.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paysera.currencyexchanger.model.db.dao.BalanceDao
import com.paysera.currencyexchanger.model.db.dao.CurrencyDao
import com.paysera.currencyexchanger.model.db.entity.Balance
import com.paysera.currencyexchanger.model.db.entity.Currency
import javax.inject.Singleton

@Singleton
@Database(entities = [Currency::class, Balance::class], version = 1, exportSchema = false)
abstract class CurrencyDatabase: RoomDatabase() {
    abstract fun getCurrencyDao(): CurrencyDao
    abstract fun getBalanceDao(): BalanceDao

    companion object {
        fun getDatabase(context: Context): CurrencyDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CurrencyDatabase::class.java,
                "currency_database"
            ).build()
        }
    }
}