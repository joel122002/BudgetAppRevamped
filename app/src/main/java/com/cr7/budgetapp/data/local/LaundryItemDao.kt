package com.cr7.budgetapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LaundryItemDao {
    @Query("SELECT * FROM laundry WHERE date BETWEEN :start AND :end")
    fun getBetweenDate(start: Date, end: Date): Flow<List<LaundryItem>>

    @Query("SELECT * FROM laundry WHERE uid IN (:uids)")
    suspend fun getItemsBUIDs(uids: List<String>): List<LaundryItem>

    @Query("SELECT * FROM laundry WHERE sync=0")
    fun getUnsyncedItems(): List<LaundryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(laundryItem: LaundryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMany(laundryItems: List<LaundryItem>)

    @Query("DELETE FROM laundry WHERE uid NOT IN (:uids) AND sync=1 AND date BETWEEN :start AND :end")
    suspend fun deleteRecordsNotInFirebase(uids: List<String>, start: Date, end: Date)

    @Update
    fun update(laundryItem: LaundryItem)

    @Delete
    fun delete(laundryItem: LaundryItem)
}