package com.paysera.currencyexchanger.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Currency", indices = [Index("currency", unique = true)])
data class Currency(
    @ColumnInfo(name = "currency") val currency: String? = null,
    @ColumnInfo(name = "rate") val rate: Double? = null
) {
    @PrimaryKey(autoGenerate = true) var id = 0
}
