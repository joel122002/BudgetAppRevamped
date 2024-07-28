package com.cr7.budgetapp.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseAuthenticatedUser {
    companion object {
        private val auth = Firebase.auth
        private val _firebaseUserState: MutableStateFlow<FirebaseUser?> = MutableStateFlow(auth.currentUser)
        val firebaseUserState: StateFlow<FirebaseUser?> = _firebaseUserState.asStateFlow()
        init {
            auth.addAuthStateListener { user ->
                _firebaseUserState.value = user.currentUser
            }
        }

        fun signInWithGoogle(idToken: String) {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            // Sign into firebase with the acquired credentials
            auth.signInWithCredential(firebaseCredential).addOnCompleteListener(
                { task ->
                    if (!task.isSuccessful) {
                        throw Throwable("Sign in failed ")
                    }
                })
        }

        fun signOut() {
            auth.signOut()
        }
    }
}