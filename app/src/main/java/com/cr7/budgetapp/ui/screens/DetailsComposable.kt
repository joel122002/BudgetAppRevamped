package com.cr7.budgetapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cr7.budgetapp.ui.theme.BudgetAppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.cr7.budgetapp.data.local.User
import com.cr7.budgetapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun DetailsScreen(authViewModel: AuthViewModel) {
    var username by remember {
        mutableStateOf("")
    }

    val coroutineScope = rememberCoroutineScope()

    BudgetAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") })
                Button(onClick = {
                    coroutineScope.launch {
                        val user = User(username.trim())
                        authViewModel.setUserDoc(user)
                    }
                }) {
                    Text(text = "Proceed")
                }
            }
        }
    }
}