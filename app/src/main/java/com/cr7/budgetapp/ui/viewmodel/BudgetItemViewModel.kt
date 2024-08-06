package com.cr7.budgetapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cr7.budgetapp.data.BudgetItemRepository
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

val TAG = "BudgetItemViewModel"
class BudgetItemViewModel(application: Application): AndroidViewModel(application) {
    private val budgetItemRepository: BudgetItemRepository
    private val _budgetItems = MutableStateFlow<List<BudgetItem>>(emptyList())
    var budgetItems: Flow<List<BudgetItem>> = _budgetItems.asStateFlow()
    private val _allUsersBudgetItems = MutableStateFlow<List<BudgetItem>>(emptyList())
    var allUsersBudgetItems: Flow<List<BudgetItem>> = _allUsersBudgetItems.asStateFlow()
    var budgetJob: Job? = null
    var calculationJob: Job? = null
    private var end: Date
    private var start: Date

    init {
        val budgetItemDao = AppDatabase.getDatabase(application).budgetItemDao()
        budgetItemRepository = BudgetItemRepository(budgetItemDao)
        end = Date()
        start = Date(end.toInstant().toEpochMilli() - (86400000*5))
    }

    fun changeDate(start: Date, end:Date) {
        this.start = start
        this.end = end
        // Cancel existing listeners if any
        budgetJob?.cancel()
        budgetJob = viewModelScope.launch {
            refreshData()
            budgetItemRepository.getData(start, end).collectLatest  { items ->
                _budgetItems.value = items
            }
        }
    }

    fun changeAllUserDate(start: Date, end:Date) {
        this.start = start
        this.end = end
        // Cancel existing listeners if any
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            val items = fetchAll()
            val clonedItems = items.map{it.copy()}
            _allUsersBudgetItems.value = clonedItems
        }
    }

    fun insert(budgetItem: BudgetItem) {
        viewModelScope.launch {
            budgetItemRepository.insert(budgetItem)
        }
    }

    suspend fun refreshData() {
        budgetItemRepository.refreshData(start, end)
    }

    suspend fun fetchAll(): List<BudgetItem> {
        return budgetItemRepository.fetchAllUserData(start, end)
    }
}