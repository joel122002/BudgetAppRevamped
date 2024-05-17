package com.cr7.budgetapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "budget")
data class BudgetItem (
    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "updated_at") val updatedAt: Date?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") val price: Float,
    // Determines if has been updated with server
    @ColumnInfo(name = "sync") val sync: Boolean = false
)