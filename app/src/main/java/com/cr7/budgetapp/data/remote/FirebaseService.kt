package com.cr7.budgetapp.data.remote

import android.util.Log
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

val TAG = "FirebaseService"

class FirebaseService {
    val db = Firebase.firestore

    suspend fun getBudgetItemsBetweenDate(start: Date, end: Date): List<BudgetItem> {
        val user = FirebaseAuthenticatedUser.firebaseUserState.value!!
        val docRef = db.collection("rooms").document("y2XLP1isVO2qbNHlNP3G").collection("budget")
            .whereEqualTo("userDoc", db.collection("users").document(user.uid))
            .whereGreaterThanOrEqualTo("date", start).whereLessThan("date", end)
        val document = docRef.get().await()
        Log.i(TAG, document.toObjects(BudgetItem::class.java).toString())
        return document.toObjects(BudgetItem::class.java)

    }

    suspend fun getUserDocument(): DocumentSnapshot? {
        val user = FirebaseAuthenticatedUser.firebaseUserState.value!!
        val docRef = db.collection("users").document(user.uid)
        val document = docRef.get().await()
        return document
    }

    suspend fun setUserDocument(user: User): DocumentSnapshot {
        val currentUser = FirebaseAuthenticatedUser.firebaseUserState.value!!
        db.collection("users").document(currentUser.uid).set(user)
        val docRef = db.collection("users").document(currentUser.uid)
        val document = docRef.get().await()
        return document
    }

    fun insertBudgetItem(budgetItem: BudgetItem) {
        val user = FirebaseAuthenticatedUser.firebaseUserState.value!!
        val a = db.collection("users").document(user.uid)
        db.collection("rooms").document("y2XLP1isVO2qbNHlNP3G").collection("budget")
            .document(budgetItem.uid).set(budgetItem)
    }
}