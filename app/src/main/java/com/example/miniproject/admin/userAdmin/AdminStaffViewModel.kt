package com.example.miniproject.admin.userAdmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.miniproject.data.SearchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminStaffViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val historyKey = "staff_search_history"

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    init {
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onClearHistoryItem(item: String) {
        historyRepository.clearHistoryItem(historyKey, item)
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun addSearchToHistory(searchTerm: String) {
        historyRepository.addToHistory(historyKey, searchTerm)
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun clearAllHistory() {
        historyRepository.clearAllHistory(historyKey)
        _searchHistory.value = emptyList()
    }
}
