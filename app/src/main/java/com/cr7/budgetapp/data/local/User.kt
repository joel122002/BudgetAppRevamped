package com.cr7.budgetapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "user")
data class User(
    var username: String,
    @Exclude @PrimaryKey var path: String
) {
    constructor(): this("", "")
    constructor(path: String): this("", path)
}