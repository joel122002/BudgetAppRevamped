package com.cr7.budgetapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentReference
import java.util.Date
import java.util.UUID

@Entity(tableName = "budget")
data class BudgetItem (
    @PrimaryKey var uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created_at") var createdAt: Date,
    @ColumnInfo(name = "updated_at") var updatedAt: Date,
    @ColumnInfo(name = "date") var date: Date,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "price") var price: Float,
    @Ignore var userDoc: DocumentReference?,
    // Determines if has been updated with server
    @ColumnInfo(name = "sync") var sync: Boolean = false
) {
    constructor(): this("",Date(), Date(), Date(), "", 0.0f, null,false)
    constructor(createdAt: Date, updatedAt: Date, date: Date, name: String, price: Float, userDoc: DocumentReference?, sync: Boolean): this(UUID.randomUUID().toString(),createdAt, updatedAt, date, name, price, userDoc,sync)
}