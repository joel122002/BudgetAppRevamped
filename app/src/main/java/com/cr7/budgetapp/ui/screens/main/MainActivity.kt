package com.cr7.budgetapp.ui.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.cr7.budgetapp.ui.screens.DetailsScreen
import com.cr7.budgetapp.ui.screens.signin.SignInScreen
import com.cr7.budgetapp.ui.viewmodel.AuthViewModel

val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = AuthViewModel()

        setContent {

            val userState by authViewModel.authState.collectAsState()
            val userDoc by authViewModel.userDoc.collectAsState()
            LaunchedEffect(userState) {
                if (userState != null) {
                    authViewModel.getUserDoc()
                }
            }
            if (userState != null) {
                if (userDoc != null && !userDoc!!.exists()) {
                    DetailsScreen(authViewModel = authViewModel)
                } else {
                    MainComposable(application = application, lifecycleScope = lifecycleScope, /*budgetItemViewModel = budgetItemViewModel*/)
                }
            } else {
                SignInScreen(activityContext = this, lifecycleScope = lifecycleScope, authViewModel = authViewModel)
            }

        }
    }
}
