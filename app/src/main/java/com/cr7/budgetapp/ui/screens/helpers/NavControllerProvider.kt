package com.cr7.budgetapp.ui.screens.helpers

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import com.cr7.budgetapp.ui.viewmodel.AuthViewModel

val LocalNavController = compositionLocalOf<NavController> { error("NavController not provided") }
val LocalApplication = compositionLocalOf<Application> { error("Application not provided") }
val LocalAuthViewModel = compositionLocalOf<AuthViewModel> { error("AuthViewModel not provided") }