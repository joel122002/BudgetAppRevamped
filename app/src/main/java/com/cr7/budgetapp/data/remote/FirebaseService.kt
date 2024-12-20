package com.cr7.budgetapp.data.remote

import android.util.Log
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.LaundryItem
import com.cr7.budgetapp.data.local.User
import com.cr7.budgetapp.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

val TAG = "FirebaseService"

const val ROOM = BuildConfig.ROOM_NAME
class FirebaseService {
    val db = Firebase.firestore

    suspend fun getBudgetItemsBetweenDate(start: Date, end: Date): List<BudgetItem> {
        val user = FirebaseAuthenticatedUser.firebaseUserState.value!!
        val docRef = db.collection("rooms").document(ROOM).collection("budget")
            .whereEqualTo("userDoc", db.collection("users").document(user.uid))
            .whereGreaterThanOrEqualTo("date", start).whereLessThan("date", end)
        val document = docRef.get().await()
        Log.i(TAG, document.toObjects(BudgetItem::class.java).toString())
        return document.toObjects(BudgetItem::class.java)

    }

    suspend fun getLaundryItemsBetweenDate(start: Date, end: Date): List<LaundryItem> {
        val docRef = db.collection("rooms").document(ROOM).collection("laundry")
            .whereGreaterThanOrEqualTo("date", start).whereLessThan("date", end)
        val document = docRef.get().await()
        Log.i(TAG, document.toObjects(LaundryItem::class.java).toString())
        return document.toObjects(LaundryItem::class.java)

    }

    suspend fun getAllBudgetItemsBetweenDate(start: Date, end: Date): List<BudgetItem> {
        val docRef = db.collection("rooms").document(ROOM).collection("budget")
            .whereGreaterThanOrEqualTo("date", start).whereLessThanOrEqualTo("date", end)
        val document = docRef.get().await()
        Log.i(TAG, document.toObjects(BudgetItem::class.java).toString())
        return document.toObjects(BudgetItem::class.java)

    }

    suspend fun getUserDocument(): DocumentSnapshot? {
        val user = FirebaseAuthenticatedUser.firebaseUserState.value!!
        val docRef = db.collection("users").document(user.uid)
        try {
            val document = docRef.get().await()
            return document
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            return null
        }

    }

    fun getUserDocumentReference(firebaseUser: FirebaseUser): DocumentReference {
        val docRef = db.collection("users").document(firebaseUser.uid)
        return docRef
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
        db.collection("rooms").document(ROOM).collection("budget")
            .document(budgetItem.uid).set(budgetItem)
    }

    suspend fun insertLaundryItem(laundryItem: LaundryItem) {
        db.collection("rooms").document(ROOM).collection("laundry")
            .document(laundryItem.uid).set(laundryItem).await()
    }

    suspend fun deleteBudgetItem(budgetItem: BudgetItem) {
        db.collection("rooms").document(ROOM).collection("budget").document(budgetItem.uid).delete().await()
    }

    suspend fun deleteLaundryItem(laundryItem: LaundryItem) {
        db.collection("rooms").document(ROOM).collection("laundry").document(laundryItem.uid).delete().await()
    }

    suspend fun resolveUsername(user: User): User {
        val query = db.document(user.path)
        val result = query.get().await()
        user.username = result.data!!.get("username").toString()
        return user
    }
}