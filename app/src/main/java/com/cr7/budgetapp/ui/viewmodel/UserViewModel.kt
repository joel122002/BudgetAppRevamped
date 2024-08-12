package com.cr7.budgetapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cr7.budgetapp.data.UserRepository
import com.cr7.budgetapp.data.local.AppDatabase
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val userRepository: UserRepository
    private val _users = MutableStateFlow<List<User>>(emptyList())
    private val _usersAsHashmap = MutableStateFlow<Map<String, String>>(emptyMap())
    var users: Flow<List<User>> = _users.asStateFlow()
    var usersAsHashmap: Flow<Map<String, String>> = _usersAsHashmap.asStateFlow()

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
        viewModelScope.launch {
            userRepository.getUsers().collectLatest { users ->
                _users.value = users
                val userMap: MutableMap<String, String> = mutableMapOf()
                users.forEach { user ->
                    userMap[user.path] = user.username
                }
                _usersAsHashmap.value = userMap
            }
        }
    }

    suspend fun resolveUsers(budgetItems: List<BudgetItem>) {
        val unresolvedUsers = mutableListOf<User>()
        val userMap = usersAsHashmap.first()
        budgetItems.forEach { budgetItem ->
            val a = userMap[budgetItem.userDoc!!.path]
            if (a.isNullOrEmpty()) {
                unresolvedUsers.add(User(path = budgetItem.userDoc!!.path))
            }
        }
        userRepository.resolveUsers(unresolvedUsers)
    }
}