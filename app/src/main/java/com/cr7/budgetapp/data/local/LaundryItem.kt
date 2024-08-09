package com.cr7.budgetapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentReference
import java.util.Date
import java.util.UUID

@Entity(tableName = "laundry")
data class LaundryItem (
    @PrimaryKey var uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created_at") var createdAt: Date,
    @ColumnInfo(name = "updated_at") var updatedAt: Date,
    @ColumnInfo(name = "date") var date: Date,
    @ColumnInfo(name = "items") var items: Int,
    @Ignore var userDoc: DocumentReference?,
    // Determines if has been updated with server
    @ColumnInfo(name = "sync") var sync: Boolean = false
) {
    constructor(): this(UUID.randomUUID().toString(), Date(), Date(), Date(), 0, null, false)
    constructor(createdAt: Date, updatedAt: Date, date: Date, items: Int, userDoc: DocumentReference?, sync: Boolean): this(
        UUID.randomUUID().toString(),createdAt, updatedAt, date, items, userDoc, sync)
}