package com.cr7.budgetapp.data.remote

import android.util.Log
import androidx.lifecycle.LiveData
import com.cr7.budgetapp.data.local.BudgetItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

val TAG = "FirebaseService"
class FirebaseService {
    val db = Firebase.firestore

    suspend fun getBudgetItemsBetweenDate (start: Date, end: Date): List<BudgetItem> {
        val docRef = db.collection("rooms").document("y2XLP1isVO2qbNHlNP3G").collection("budget").whereEqualTo("userId", "joelv1202")
        val document = docRef.get().await()
        Log.i(TAG, document.toObjects(BudgetItem::class.java).toString())
        return document.toObjects(BudgetItem::class.java)
    }

    fun insertBudgetItem(budgetItem: BudgetItem) {
        db.collection("rooms").document("y2XLP1isVO2qbNHlNP3G").collection("budget").document(budgetItem.uid).set(budgetItem)
    }
}