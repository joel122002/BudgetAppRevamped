package com.cr7.budgetapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cr7.budgetapp.data.local.User
import com.cr7.budgetapp.data.remote.FirebaseAuthenticatedUser
import com.cr7.budgetapp.data.remote.FirebaseService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    val authState: StateFlow<FirebaseUser?> = FirebaseAuthenticatedUser.firebaseUserState
    private val _userDoc  = MutableStateFlow<DocumentSnapshot?>(null)
    val userDoc: StateFlow<DocumentSnapshot?> = _userDoc.asStateFlow()
    private val firebaseService: FirebaseService


    init {
        firebaseService = FirebaseService()
        viewModelScope.launch {
            authState.collect({
                if (authState.value != null) {
                    _userDoc.value = firebaseService.getUserDocument()
                }
            })
        }
    }

    fun getUserDocumentRef(): DocumentReference {
        return firebaseService.getUserDocumentRefernce()
    }
    fun signInWithGoogle(idToken: String) {
        FirebaseAuthenticatedUser.signInWithGoogle(idToken)
    }

    suspend fun getUserDoc(): DocumentSnapshot? {
        val userDoc = firebaseService.getUserDocument()
        _userDoc.value = userDoc
        return userDoc
    }

    suspend fun setUserDoc(user: User): DocumentSnapshot {
        val doc = firebaseService.setUserDocument(user)
        _userDoc.value = doc
        return doc
    }

    fun signOut() {
        FirebaseAuthenticatedUser.signOut()
    }
}