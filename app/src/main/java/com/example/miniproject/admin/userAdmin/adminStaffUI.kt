package com.example.miniproject.admin.userAdmin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.components.SearchResultList
import com.example.miniproject.components.SearchScreen
import kotlinx.coroutines.launch

@Composable
fun AdminStaffScreen(
    navController: NavController,
    viewModel: AdminStaffViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var showModifyUserDialog by remember { mutableStateOf(false) }
    var modifyUserName by remember { mutableStateOf("") }
    var modifyUserEmail by remember { mutableStateOf("") }
    var modifyUserDisplayId by remember { mutableStateOf("") }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddUserDialog = true
                },
                containerColor = Color(0xFF5553DC),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Staff", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            SearchScreen(
                title = "Staff",
                searchPlaceholder = "Enter staff display ID (e.g., S1, S2, S3)...",
                searchText = searchText,
                onSearchTextChange = viewModel::onSearchTextChange,
                searchHistory = searchHistory,
                onClearHistoryItem = viewModel::onClearHistoryItem,
                onClearAllHistory = viewModel::clearAllHistory,
                onSearch = {
                    viewModel.onSearch()
                },
                onBackClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                content = {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF5553DC))
                            }
                        }
                        searchResults == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Enter a staff display ID to search",
                                    color = Color.Gray
                                )
                            }
                        }
                        searchResults?.isEmpty() == true && searchText.isNotBlank() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No staff found with ID: $searchText",
                                    color = Color.Gray
                                )
                            }
                        }
                        else -> {
                            SearchResultList(
                                results = searchResults,
                                onEditItem = { userId ->
                                    // Fetch user data and show modify dialog
                                    viewModel.getUserForEdit(
                                        userId = userId,
                                        onSuccess = { userData ->
                                            modifyUserName = userData.name
                                            modifyUserEmail = userData.email
                                            modifyUserDisplayId = userData.displayId
                                            showModifyUserDialog = true
                                        },
                                        onError = { errorMessage ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(errorMessage)
                                            }
                                        }
                                    )
                                },
                                onDeleteItem = { userId ->
                                    viewModel.deleteUser(userId)
                                }
                            )
                        }
                    }
                }
            )

            // Add User Dialog
            AddUserDialog(
                userType = "staff",
                showDialog = showAddUserDialog,
                onDismiss = { showAddUserDialog = false },
                onAddUser = { name, email, password, displayId ->
                    viewModel.addUser(
                        name = name,
                        email = email,
                        password = password,
                        displayId = displayId,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Staff added successfully")
                                showAddUserDialog = false
                                viewModel.clearSearchResults()
                            }
                        },
                        onError = { errorMessage ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to add staff: $errorMessage")
                            }
                        }
                    )
                },
                isLoading = false,
                error = null,
                generateDisplayId = { _ ->
                    viewModel.generateStaffDisplayId()
                }
            )

            // Modify User Dialog
            AddUserDialog(
                userType = "staff",
                showDialog = showModifyUserDialog,
                onDismiss = {
                    showModifyUserDialog = false
                    modifyUserName = ""
                    modifyUserEmail = ""
                    modifyUserDisplayId = ""
                },
                onAddUser = { _, _, _, _ -> }, // Required but not used in modify mode
                onUpdateUser = { name, email, displayId ->
                    viewModel.updateUser(
                        displayId = displayId,
                        name = name,
                        email = email,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Staff updated successfully")
                                showModifyUserDialog = false
                                modifyUserName = ""
                                modifyUserEmail = ""
                                modifyUserDisplayId = ""
                                // Refresh search results
                                if (searchText.isNotBlank()) {
                                    viewModel.onSearch()
                                }
                            }
                        },
                        onError = { errorMessage ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to update staff: $errorMessage")
                            }
                        }
                    )
                },
                onSendPasswordReset = { email ->
                    viewModel.sendPasswordReset(
                        email = email,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Password reset email sent to $email")
                            }
                        },
                        onError = { errorMessage ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to send reset email: $errorMessage")
                            }
                        }
                    )
                },
                isLoading = false,
                isSendingReset = false,
                error = null,
                generateDisplayId = { _ ->
                    viewModel.generateStaffDisplayId()
                },
                isModifyMode = true,
                existingUser = if (modifyUserDisplayId.isNotEmpty()) {
                    ExistingUserData(
                        name = modifyUserName,
                        email = modifyUserEmail,
                        displayId = modifyUserDisplayId
                    )
                } else null
            )
        }
    }
}