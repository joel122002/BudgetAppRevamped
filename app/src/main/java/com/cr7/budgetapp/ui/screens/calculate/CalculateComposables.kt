package com.cr7.budgetapp.ui.screens.calculate

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cr7.budgetapp.ui.screens.helpers.LocalNavController
import com.cr7.budgetapp.ui.theme.BudgetAppTheme

@Composable
fun CalculateScreen() {
    BudgetAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppBar {
                Text(text = "Test")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(content: @Composable() (innerPadding: PaddingValues) -> Unit) {
    var dropdownOpen by remember {
        mutableStateOf(false)
    }
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Budget App")
                },
                actions = {
                },
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}