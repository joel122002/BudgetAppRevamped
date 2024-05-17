package com.cr7.budgetapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface BudgetItemDao {
    @Query("SELECT * FROM budget WHERE created_at BETWEEN :start AND :end")
    fun getBetweenDate(start: Date, end: Date): LiveData<BudgetItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(budgetItem: BudgetItem)

    @Update
    fun update(budgetItem: BudgetItem)

    @Delete
    fun delete(budgetItem: BudgetItem)
}