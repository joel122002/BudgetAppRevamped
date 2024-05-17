package com.cr7.budgetapp.data

import androidx.lifecycle.LiveData
import java.util.Date

class BudgetItemRepository(private val budgetItemDao: BudgetItemDao) {
    suspend fun getData(start: Date, end: Date): LiveData<BudgetItem> {
        return budgetItemDao.getBetweenDate(start, end)
    }

    suspend fun refreshData() {
        var remoteData:LiveData<BudgetItem>
//        remoteData.forEach { budgetItemDao.insert(it) }
    }

    suspend fun insert(budgetItem: BudgetItem) {
        budgetItemDao.insert(budgetItem)
    }

    suspend fun delete(budgetItem: BudgetItem) {
        budgetItemDao.delete(budgetItem)
    }
}