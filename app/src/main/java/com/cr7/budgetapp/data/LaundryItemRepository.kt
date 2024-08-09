package com.cr7.budgetapp.data

import com.cr7.budgetapp.data.local.LaundryItem
import com.cr7.budgetapp.data.local.LaundryItemDao
import com.cr7.budgetapp.data.remote.FirebaseAuthenticatedUser
import com.cr7.budgetapp.data.remote.FirebaseService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date

class LaundryItemRepository(private val laundryItemDao: LaundryItemDao) {
    private val firebaseService = FirebaseService()
    fun getData(start: Date, end: Date): Flow<List<LaundryItem>> {
        return laundryItemDao.getBetweenDate(start, end)
    }

    /**
     * Function to insert new records and to update records that already exist with the record that has the newest updated_at
     */
    suspend fun refreshData(start: Date, end: Date) {
        // Server Items. Has all items that would exist in existingItems
        val laundryItems = firebaseService.getLaundryItemsBetweenDate(start, end)
        val ids = laundryItems.map { it.uid }
        // Database Items. Subset of budgetItems
        val existingItems = laundryItemDao.getItemsBUIDs(ids)
        val existingItemsMap = existingItems.associateBy { it.uid }

        // Creates a list of elements that either
        // 1. Do not exist in the database
        // 2. If exists chooses the item with the larger updatedAt
        val insertOrUpdateItems = laundryItems.map {
            if (existingItemsMap[it.uid]?.uid != it.uid)
                it
            else
                (if (existingItemsMap[it.uid]?.updatedAt!! > it.updatedAt)
                    existingItemsMap[it.uid]!!
                else
                    it)
        }

        withContext(Dispatchers.IO) {
            laundryItemDao.insertMany(insertOrUpdateItems)
            laundryItemDao.deleteRecordsNotInFirebase(ids, start, end)
            val unsyncedItems = laundryItemDao.getUnsyncedItems()
            unsyncedItems.forEach { laundryItem ->
                laundryItem.sync = true
                laundryItem.userDoc =
                    Firebase.firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
                firebaseService.insertLaundryItem(laundryItem)
                laundryItemDao.insert(laundryItem)
            }
        }
    }

    suspend fun insert(laundryItem: LaundryItem) {
        withContext(Dispatchers.IO) {
            laundryItemDao.insert(laundryItem)
            laundryItem.sync = true
            firebaseService.insertLaundryItem(laundryItem)
            laundryItemDao.insert(laundryItem)
        }
    }

    suspend fun delete(laundryItem: LaundryItem) {
        withContext(Dispatchers.IO) {
            firebaseService.deleteLaundryItem(laundryItem)
            laundryItemDao.delete(laundryItem)
        }
    }
}