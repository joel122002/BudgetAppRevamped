package com.cr7.budgetapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cr7.budgetapp.data.local.BudgetItem
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BudgetItemDao {
    @Query("SELECT * FROM budget WHERE date BETWEEN :start AND :end")
    fun getBetweenDate(start: Date, end: Date): Flow<List<BudgetItem>>

    @Query("SELECT * FROM budget WHERE uid IN (:uids)")
    suspend fun getItemsBUIDs(uids: List<String>): List<BudgetItem>

    @Query("SELECT * FROM budget WHERE sync=0")
    fun getUnsyncedItems(): List<BudgetItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(budgetItem: BudgetItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMany(budgetItems: List<BudgetItem>)

    @Query("DELETE FROM budget WHERE uid NOT IN (:uids) AND sync=1 AND date BETWEEN :start AND :end")
    suspend fun deleteRecordsNotInFirebase(uids: List<String>, start: Date, end: Date)

    @Update
    fun update(budgetItem: BudgetItem)

    @Delete
    fun delete(budgetItem: BudgetItem)
}