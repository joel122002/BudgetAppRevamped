package com.cr7.budgetapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cr7.budgetapp.data.LaundryItemRepository
import com.cr7.budgetapp.data.local.AppDatabase
import com.cr7.budgetapp.data.local.LaundryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class LaundryItemViewModel(application: Application): AndroidViewModel(application) {
    private val laundryItemRepository: LaundryItemRepository
    private val _laundryItems = MutableStateFlow<List<LaundryItem>>(emptyList())
    var laundryItems: Flow<List<LaundryItem>> = _laundryItems.asStateFlow()
    var calculationJob: Job? = null
    private var end: Date
    private var start: Date

    init {
        val laundryItemDao = AppDatabase.getDatabase(application).laundryItemDao()
        laundryItemRepository = LaundryItemRepository(laundryItemDao)
        end = Date()
        start = Date(end.toInstant().toEpochMilli() - (86400000*5))
    }

    fun changeDate(start: Date, end: Date) {
        this.start = start
        this.end = end
        // Cancel existing listeners if any
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            refreshData()
            laundryItemRepository.getData(start, end).collectLatest  { items ->
                _laundryItems.value = items
            }
        }
    }

    fun insert(laundryItem: LaundryItem) {
        viewModelScope.launch {
            laundryItemRepository.insert(laundryItem)
        }
    }

    fun delete(laundryItem: LaundryItem) {
        viewModelScope.launch {
            laundryItemRepository.delete(laundryItem)
        }
    }

    suspend fun refreshData() {
        laundryItemRepository.refreshData(start, end)
    }
}