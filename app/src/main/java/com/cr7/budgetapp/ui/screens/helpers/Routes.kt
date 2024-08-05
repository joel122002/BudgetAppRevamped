package com.cr7.budgetapp.ui.screens.helpers

enum class Routes(val route: String) {
    calculate("calculate"),
    budget("budget"),
    laundry("laundry");

    companion object {
        fun fromString(route: String) = entries.first { it.route == route }
    }
}