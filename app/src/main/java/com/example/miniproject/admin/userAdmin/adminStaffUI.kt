package com.example.miniproject.admin.userAdmin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.components.SearchScreen

@Composable
fun AdminStaffScreen(
    navController: NavController,
    viewModel: AdminStaffViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    SearchScreen(
        title = "Staff",
        searchPlaceholder = "Enter staff name...",
        searchText = searchText,
        onSearchTextChange = viewModel::onSearchTextChange,
        searchHistory = searchHistory,
        onClearHistoryItem = viewModel::onClearHistoryItem,
        onClearAllHistory = viewModel::clearAllHistory,
        onSearch = { 
            viewModel.addSearchToHistory(searchText)
            // TODO: Implement actual search logic 
        },
        onBackClick = { navController.popBackStack() }
    )
}
