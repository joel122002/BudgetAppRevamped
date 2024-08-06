package com.cr7.budgetapp.ui.screens.helpers

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> { error("NavController not provided") }
val LocalApplication = compositionLocalOf<Application> { error("Application not provided") }