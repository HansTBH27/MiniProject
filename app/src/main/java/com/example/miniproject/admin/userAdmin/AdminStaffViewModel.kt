package com.example.miniproject.admin.userAdmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.components.SearchResultItemData
import com.example.miniproject.data.SearchHistoryRepository
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(FlowPreview::class)
class AdminStaffViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val historyKey = "staff_search_history"

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItemData>?>(null)
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var isManualSearch = false

    init {
        _searchHistory.value = historyRepository.getHistory(historyKey)

        viewModelScope.launch {
            searchText
                .debounce(500)
                .collect { query ->
                    if (!isManualSearch && query.isNotBlank()) {
                        performSearch(query)
                    }
                    isManualSearch = false
                }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        isManualSearch = false
    }

    fun getUserForEdit(
        userId: String,
        onSuccess: (ExistingUserData) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("user")
                    .document(userId)
                    .get()
                    .await()

                if (document.exists()) {
                    val userData = ExistingUserData(
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        displayId = document.getString("displayId") ?: ""
                    )
                    onSuccess(userData)
                } else {
                    onError("User not found")
                }
            } catch (e: Exception) {
                onError("Error loading user: ${e.message}")
            }
        }
    }

    fun onSearch() {
        val query = _searchText.value.trim()
        if (query.isNotBlank()) {
            isManualSearch = true
            addSearchToHistory(query)
            performSearch(query)
        } else {
            _searchResults.value = null
        }
    }

    fun updateUser(
        displayId: String,
        name: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Find user by displayId
                val userQuery = firestore.collection("user")
                    .whereEqualTo("displayId", displayId)
                    .whereEqualTo("role", "staff")
                    .get()
                    .await()

                if (userQuery.documents.isEmpty()) {
                    onError("Staff member not found")
                    return@launch
                }

                val userId = userQuery.documents[0].id

                // Update Firestore document (name only)
                firestore.collection("user")
                    .document(userId)
                    .update("name", name)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update staff member")
            }
        }
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to send password reset email")
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val exactMatchResults = searchExactMatch(query)

                if (exactMatchResults.isNotEmpty()) {
                    _searchResults.value = exactMatchResults
                } else {
                    searchWithBroadMatch(query)
                }

                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _isLoading.value = false
                _searchResults.value = emptyList()
            }
        }
    }

    private suspend fun searchExactMatch(query: String): List<SearchResultItemData> {
        return try {
            val querySnapshot = firestore.collection("user")
                .whereEqualTo("displayId", query)
                .whereEqualTo("role", "staff")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: return@mapNotNull null
                val name = document.getString("name") ?: "Unknown"
                val email = document.getString("email") ?: ""

                SearchResultItemData(
                    id = document.id,
                    title = "$name ($displayId)",
                    subtitle = email
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun searchWithBroadMatch(query: String) {
        try {
            val querySnapshot = firestore.collection("user")
                .whereEqualTo("role", "staff")
                .get()
                .await()

            val results = querySnapshot.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: "No ID"
                val name = document.getString("name") ?: ""
                val email = document.getString("email") ?: "No Email"

                val searchQuery = query.lowercase(Locale.getDefault())
                val matches = displayId.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        name.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        email.lowercase(Locale.getDefault()).contains(searchQuery)

                if (matches) {
                    SearchResultItemData(
                        id = document.id,
                        title = "$name ($displayId)",
                        subtitle = email
                    )
                } else null
            }.sortedBy {
                try {
                    val idText = it.title.substringAfter("(").substringBefore(")")
                    idText.replace("S", "", ignoreCase = true).toIntOrNull() ?: Int.MAX_VALUE
                } catch (e: Exception) {
                    Int.MAX_VALUE
                }
            }

            _searchResults.value = results
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        }
    }

    suspend fun generateStaffDisplayId(): String {
        return try {
            val users = firestore.collection("user")
                .whereEqualTo("role", "staff")
                .get()
                .await()

            val staffIds = users.documents.mapNotNull { document ->
                document.getString("displayId")
            }

            var maxId = 0
            staffIds.forEach { id ->
                try {
                    val numericPart = id.replace(Regex("[^0-9]"), "")
                    if (numericPart.isNotEmpty()) {
                        val num = numericPart.toInt()
                        if (num > maxId) {
                            maxId = num
                        }
                    }
                } catch (e: Exception) {
                    // Skip if not numeric
                }
            }

            val newId = maxId + 1
            "S$newId"

        } catch (e: Exception) {
            "S1001"
        }
    }

    fun addUser(
        name: String,
        email: String,
        password: String,
        displayId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val formattedDisplayId = if (!displayId.startsWith("S", ignoreCase = true)) {
                    "S${displayId.uppercase(Locale.getDefault())}"
                } else {
                    displayId.uppercase(Locale.getDefault())
                }

                // Check if display ID already exists in Firestore
                val existingIdQuery = firestore.collection("user")
                    .whereEqualTo("displayId", formattedDisplayId)
                    .get()
                    .await()

                if (!existingIdQuery.isEmpty) {
                    onError("Staff with ID $formattedDisplayId already exists")
                    return@launch
                }

                // Check if email already exists in Firestore
                val existingEmailQuery = firestore.collection("user")
                    .whereEqualTo("email", email.lowercase(Locale.getDefault()))
                    .get()
                    .await()

                if (!existingEmailQuery.isEmpty) {
                    onError("Staff with email $email already exists")
                    return@launch
                }

                // Create user in Firebase Authentication
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user

                if (user == null) {
                    onError("Failed to create authentication user")
                    return@launch
                }

                // Update user profile with name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Store user data in Firestore using Auth UID
                val staffData = hashMapOf(
                    "displayId" to formattedDisplayId,
                    "name" to name,
                    "email" to email.lowercase(Locale.getDefault()),
                    "role" to "staff"
                )

                firestore.collection("user")
                    .document(user.uid)
                    .set(staffData)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onError("Failed to add staff: ${e.message}")
            }
        }
    }

    fun onClearHistoryItem(item: String) {
        historyRepository.clearHistoryItem(historyKey, item)
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun addSearchToHistory(searchTerm: String) {
        if (searchTerm.isNotBlank()) {
            historyRepository.addToHistory(historyKey, searchTerm)
            _searchHistory.value = historyRepository.getHistory(historyKey)
        }
    }

    fun clearAllHistory() {
        historyRepository.clearAllHistory(historyKey)
        _searchHistory.value = emptyList()
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("user")
                    .document(userId)
                    .delete()
                    .await()

                _searchResults.value = _searchResults.value?.filter { it.id != userId }

            } catch (e: Exception) {
                _error.value = "Failed to delete user: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSearchResults() {
        _searchResults.value = null
    }
}