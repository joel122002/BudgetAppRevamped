package com.cr7.budgetapp.data

import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.BudgetItemDao
import com.cr7.budgetapp.data.remote.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date

class BudgetItemRepository(
    private val budgetItemDao: BudgetItemDao
) {
    private val firebaseService = FirebaseService()
    fun getData(start: Date, end: Date): Flow<List<BudgetItem>> {
        return budgetItemDao.getBetweenDate(start, end)
    }

    /**
     * Function to insert new records and to update records that already exist with the record that has the newest updated_at
     */
    suspend fun refreshData(start: Date, end: Date) {
        // Server Items. Has all items that would exist in existingItems
        val budgetItems = firebaseService.getBudgetItemsBetweenDate(start, end)
        val ids = budgetItems.map { it.uid }
        // Database Items. Subset of budgetItems
        val existingItems = budgetItemDao.getItemsBUIDs(ids)
        val existingItemsMap = existingItems.associateBy { it.uid }

        // Creates a list of elements that either
        // 1. Do not exist in the database
        // 2. If exists chooses the item with the larger updatedAt
        val insertOrUpdateItems = budgetItems.map {
            if (existingItemsMap[it.uid]?.uid != it.uid)
                it
            else
                    (if (existingItemsMap[it.uid]?.updatedAt!! > it.updatedAt)
                        existingItemsMap[it.uid]!!
                    else
                        it)
        }

        withContext(Dispatchers.IO) {
            budgetItemDao.insertMany(insertOrUpdateItems)
            budgetItemDao.deleteRecordsNotInFirebase(ids, start, end)
            val unsyncedItems = budgetItemDao.getUnsyncedItems()
            unsyncedItems.forEach { budgetItem ->
                budgetItem.sync = true
                firebaseService.insertBudgetItem(budgetItem)
                budgetItemDao.insert(budgetItem)
            }
        }
    }

    suspend fun insert(budgetItem: BudgetItem) {
        withContext(Dispatchers.IO) {
            budgetItemDao.insert(budgetItem)
            budgetItem.sync = true
            firebaseService.insertBudgetItem(budgetItem)
            budgetItemDao.insert(budgetItem)
        }
    }

    suspend fun delete(budgetItem: BudgetItem) {
        budgetItemDao.delete(budgetItem)
    }
}