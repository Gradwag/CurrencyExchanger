package com.paysera.currencyexchanger.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Balance", indices = [Index("currency", unique = true)])
data class Balance(
    @ColumnInfo(name = "currency") val currency: String? = null,
    @ColumnInfo(name = "balance") val balance: Double? = null
) {
    @PrimaryKey(autoGenerate = true) var id = 0
}
