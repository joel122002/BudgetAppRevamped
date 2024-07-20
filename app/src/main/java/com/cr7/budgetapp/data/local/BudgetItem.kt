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
    @ColumnInfo(name = "updated_at") var updatedAt: Date,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "price") var price: Float,
    @ColumnInfo(name = "user_id") val userId: String,
    // Determines if has been updated with server
    @ColumnInfo(name = "sync") var sync: Boolean = false
) {
    constructor(): this("",Date(), Date(), Date(), "", 0.0f, "",false)
    constructor(createdAt: Date, updatedAt: Date, date: Date, name: String, price: Float, sync: Boolean): this(UUID.randomUUID().toString(),createdAt, updatedAt, date, name, price, "joelv1202",sync)
}