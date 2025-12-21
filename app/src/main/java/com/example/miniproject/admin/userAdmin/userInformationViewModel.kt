package com.example.miniproject.admin.userAdmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserInformationState(
    val user: UserInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)

data class EditUserState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val currentPassword: String = "", // NEW: For re-authentication when changing email
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEditDialog: Boolean = false,
    val showPasswordSection: Boolean = false,
    val emailChanged: Boolean = false // NEW: Track if email was changed
)

data class UserInfo(
    val id: String,
    val displayId: String,
    val name: String,
    val email: String,
    val role: String = "User",
    val isActive: Boolean = true,
    val requirePasswordChange: Boolean = false,
    val createdAt: String? = null
)

class UserInformationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userState = MutableStateFlow(UserInformationState())
    val userState = _userState.asStateFlow()

    private val _editState = MutableStateFlow(EditUserState())
    val editState = _editState.asStateFlow()

    fun loadUser(userId: String, userType: String) {
        viewModelScope.launch {
            try {
                _userState.value = _userState.value.copy(isLoading = true, error = null)

                // Try to get user by displayId
                val querySnapshot = firestore.collection("user")
                    .whereEqualTo("displayId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val data = document.data ?: emptyMap()

                    val user = UserInfo(
                        id = document.id,
                        displayId = data["displayId"] as? String ?: "No ID",
                        name = data["name"] as? String ?: "No Name",
                        email = data["email"] as? String ?: "No Email",
                        role = data["role"] as? String ?: userType,
                        isActive = data["isActive"] as? Boolean ?: true,
                        requirePasswordChange = data["requirePasswordChange"] as? Boolean ?: false,
                        createdAt = data["createdAt"] as? String
                    )

                    _userState.value = _userState.value.copy(
                        user = user,
                        isLoading = false
                    )
                } else {
                    // Try to get by document ID
                    val document = firestore.collection("user")
                        .document(userId)
                        .get()
                        .await()

                    if (document.exists()) {
                        val data = document.data ?: emptyMap()

                        val user = UserInfo(
                            id = document.id,
                            displayId = data["displayId"] as? String ?: "No ID",
                            name = data["name"] as? String ?: "No Name",
                            email = data["email"] as? String ?: "No Email",
                            role = data["role"] as? String ?: userType,
                            isActive = data["isActive"] as? Boolean ?: true,
                            requirePasswordChange = data["requirePasswordChange"] as? Boolean ?: false,
                            createdAt = data["createdAt"] as? String
                        )

                        _userState.value = _userState.value.copy(
                            user = user,
                            isLoading = false
                        )
                    } else {
                        _userState.value = _userState.value.copy(
                            error = "User not found",
                            isLoading = false
                        )
                    }
                }

            } catch (e: Exception) {
                _userState.value = _userState.value.copy(
                    error = "Failed to load user: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Edit Dialog Functions
    fun showEditDialog(user: UserInfo) {
        _editState.value = _editState.value.copy(
            showEditDialog = true,
            name = user.name,
            email = user.email,
            showPasswordSection = false,
            password = "",
            confirmPassword = "",
            currentPassword = "",
            emailChanged = false,
            error = null
        )
    }

    fun hideEditDialog() {
        _editState.value = _editState.value.copy(showEditDialog = false)
    }

    fun updateName(name: String) {
        _editState.value = _editState.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        val originalEmail = _userState.value.user?.email ?: ""
        _editState.value = _editState.value.copy(
            email = email,
            emailChanged = email != originalEmail
        )
    }

    fun updatePassword(password: String) {
        _editState.value = _editState.value.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _editState.value = _editState.value.copy(confirmPassword = confirmPassword)
    }

    fun updateCurrentPassword(currentPassword: String) {
        _editState.value = _editState.value.copy(currentPassword = currentPassword)
    }

    fun togglePasswordSection() {
        _editState.value = _editState.value.copy(
            showPasswordSection = !_editState.value.showPasswordSection,
            password = "",
            confirmPassword = ""
        )
    }

    fun updateUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _editState.value = _editState.value.copy(isLoading = true, error = null)

                // Validate inputs
                if (_editState.value.name.isBlank()) {
                    throw IllegalArgumentException("Name cannot be empty")
                }

                if (_editState.value.email.isBlank()) {
                    throw IllegalArgumentException("Email cannot be empty")
                }

                // Validate password change if enabled
                if (_editState.value.showPasswordSection) {
                    if (_editState.value.password.isBlank()) {
                        throw IllegalArgumentException("New password cannot be empty")
                    }
                    if (_editState.value.password != _editState.value.confirmPassword) {
                        throw IllegalArgumentException("Passwords do not match")
                    }
                    if (_editState.value.password.length < 6) {
                        throw IllegalArgumentException("Password must be at least 6 characters")
                    }
                }

                // Validate current password if email changed
                if (_editState.value.emailChanged && _editState.value.currentPassword.isBlank()) {
                    throw IllegalArgumentException("Current password is required to change email")
                }

                val originalEmail = _userState.value.user?.email ?: ""

                // STEP 1: Update Firebase Authentication if email changed
                if (_editState.value.emailChanged) {
                    // Get the Firebase Auth user
                    val authUser = auth.currentUser
                    if (authUser == null) {
                        throw IllegalStateException("No authenticated user found")
                    }

                    // Re-authenticate user with current password
                    val credential = EmailAuthProvider.getCredential(
                        originalEmail,
                        _editState.value.currentPassword
                    )

                    authUser.reauthenticate(credential).await()

                    // Update email in Firebase Auth
                    authUser.updateEmail(_editState.value.email).await()

                    // Send verification email to new address
                    authUser.sendEmailVerification().await()
                }

                // STEP 2: Update password in Firebase Auth if changed
                if (_editState.value.showPasswordSection && _editState.value.password.isNotBlank()) {
                    val authUser = auth.currentUser
                    if (authUser != null) {
                        authUser.updatePassword(_editState.value.password).await()
                    }
                }

                // STEP 3: Update Firestore
                val updates = mutableMapOf<String, Any>(
                    "name" to _editState.value.name,
                    "email" to _editState.value.email
                )

                firestore.collection("user")
                    .document(userId)
                    .update(updates)
                    .await()

                // Update local state
                val currentUser = _userState.value.user
                if (currentUser != null) {
                    _userState.value = _userState.value.copy(
                        user = currentUser.copy(
                            name = _editState.value.name,
                            email = _editState.value.email
                        )
                    )
                }

                _editState.value = _editState.value.copy(isLoading = false)
                hideEditDialog()
                onSuccess()

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                        "Current password is incorrect"
                    e.message?.contains("WEAK_PASSWORD") == true ->
                        "Password is too weak"
                    e.message?.contains("EMAIL_EXISTS") == true ->
                        "Email is already in use"
                    e.message?.contains("INVALID_EMAIL") == true ->
                        "Invalid email format"
                    else -> e.message ?: "Update failed"
                }

                _editState.value = _editState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                onError(errorMessage)
            }
        }
    }

    // Delete Dialog Functions
    fun showDeleteConfirmation() {
        _userState.value = _userState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteConfirmation() {
        _userState.value = _userState.value.copy(showDeleteDialog = false)
    }

    fun deleteUser(
        userId: String,
        userType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Delete from Firestore
                firestore.collection("user")
                    .document(userId)
                    .delete()
                    .await()

                // Note: Deleting from Firebase Auth requires admin SDK or Cloud Functions
                // This only deletes the Firestore document
                // You should set up a Cloud Function to handle Auth deletion

                onSuccess()

            } catch (e: Exception) {
                onError("Failed to delete user: ${e.message}")
            }
        }
    }
}