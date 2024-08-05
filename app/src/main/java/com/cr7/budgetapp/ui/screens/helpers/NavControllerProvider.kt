package com.cr7.budgetapp.ui.screens.helpers

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> { error("NavController not provided") }