package com.cr7.budgetapp.ui.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cr7.budgetapp.data.BudgetItemRepository
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.data.local.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

val TAG = "BudgetItemViewModel"
@RequiresApi(Build.VERSION_CODES.O)
class BudgetItemViewModel(application: Application): AndroidViewModel(application) {
    private val budgetItemRepository: BudgetItemRepository
    private val _budgetItems = MutableStateFlow<List<BudgetItem>>(emptyList())
    var budgetItems: Flow<List<BudgetItem>> = _budgetItems.asStateFlow()
    var job: Job? = null
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
        job?.cancel()
        job = viewModelScope.launch {
            refreshData()
            budgetItemRepository.getData(start, end).collectLatest  { items ->
                _budgetItems.value = items
            }
        }


        Log.i("1", "1")
    }

    fun insert(budgetItem: BudgetItem) {
        viewModelScope.launch {
            budgetItemRepository.insert(budgetItem)
        }
    }

    suspend fun refreshData() {
        budgetItemRepository.refreshData(start, end)
    }
}