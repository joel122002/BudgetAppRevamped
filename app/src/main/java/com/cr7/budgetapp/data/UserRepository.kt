package com.cr7.budgetapp.data

import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.User
import com.cr7.budgetapp.data.local.UserDao
import com.cr7.budgetapp.data.remote.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    private val firebaseService = FirebaseService()

    suspend fun resolveUsers(users: List<User>) {
        withContext(Dispatchers.IO) {
            users.forEach {user ->
                val resolvedUser = firebaseService.resolveUsername(user)
                userDao.insert(resolvedUser)
            }
        }
    }

    fun getUsers(): Flow<List<User>> {
        return userDao.getUsers()
    }
}