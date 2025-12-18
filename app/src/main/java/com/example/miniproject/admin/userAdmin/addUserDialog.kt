package com.example.miniproject.admin.userAdmin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class ExistingUserData(
    val name: String,
    val email: String,
    val displayId: String
)
@Composable
fun AddUserDialog(
    userType: String, // "student" or "staff"
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAddUser: (name: String, email: String, password: String, displayId: String) -> Unit,
    onUpdateUser: (name: String, email: String, displayId: String) -> Unit = { _, _, _ -> }, // NEW for modify
    onSendPasswordReset: (email: String) -> Unit = { _ -> }, // NEW for password reset
    isLoading: Boolean = false,
    isSendingReset: Boolean = false, // NEW loading state for reset
    error: String? = null,
    generateDisplayId: suspend (String) -> String,
    isModifyMode: Boolean = false, // NEW flag for modify mode
    existingUser: ExistingUserData? = null // NEW existing user data for modify
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayId by remember { mutableStateOf("") }
    var isGeneratingId by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) } // NEW for reset confirmation
    val scope = rememberCoroutineScope()

    // Data class for existing user


    // When dialog opens, set up fields based on mode
    LaunchedEffect(showDialog, isModifyMode, existingUser) {
        if (showDialog) {
            if (isModifyMode && existingUser != null) {
                // Populate with existing user data for modify mode
                name = existingUser.name
                email = existingUser.email
                displayId = existingUser.displayId
                password = ""
                confirmPassword = ""
                resetEmailSent = false
            } else {
                // For add mode, generate ID and clear fields
                name = ""
                email = ""
                password = ""
                confirmPassword = ""
                resetEmailSent = false
                isGeneratingId = true
                displayId = generateDisplayId(userType)
                isGeneratingId = false
            }
        }
    }

    // Calculate if add button should be enabled
    val isAddButtonEnabled = !isLoading && !isGeneratingId &&
            displayId.isNotBlank() &&
            name.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            password == confirmPassword &&
            password.length >= 6

    // Calculate if update button should be enabled
    val isUpdateButtonEnabled = !isLoading && !isSendingReset &&
            name.isNotBlank() &&
            email.isNotBlank()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading && !isGeneratingId && !isSendingReset) onDismiss()
            },
            title = {
                Text(
                    if (isModifyMode) "Modify ${userType.replaceFirstChar { it.uppercase() }}"
                    else "Add New ${userType.replaceFirstChar { it.uppercase() }}"
                )
            },
            text = {
                Column {
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Display ID field
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = displayId,
                            onValueChange = { if (!isModifyMode && !isGeneratingId) displayId = it },
                            label = {
                                Text(
                                    if (isModifyMode) "Display ID"
                                    else "Display ID (Auto-generated)"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Badge,
                                    contentDescription = "Display ID",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (isGeneratingId) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            enabled = !isModifyMode, // Can't change ID in modify mode
                            readOnly = isModifyMode || isGeneratingId
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Refresh button - only show for add mode
                        if (!isModifyMode) {
                            IconButton(
                                onClick = {
                                    if (!isGeneratingId && !isLoading && !isSendingReset) {
                                        scope.launch {
                                            isGeneratingId = true
                                            displayId = generateDisplayId(userType)
                                            isGeneratingId = false
                                        }
                                    }
                                },
                                enabled = !isGeneratingId && !isLoading && !isSendingReset
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "Generate New ID",
                                    tint = if (isGeneratingId || isLoading || isSendingReset)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = name.isBlank(),
                        enabled = !isSendingReset
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isBlank(),
                        enabled = !isSendingReset
                    )

                    // PASSWORD FIELDS - Only show for add mode
                    if (!isModifyMode) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            isError = password.isNotBlank() && password.length < 6
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Confirm Password field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirm Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            isError = confirmPassword.isNotBlank() && password != confirmPassword
                        )
                    } else {
                        // PASSWORD RESET SECTION - Only for modify mode
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Filled.VpnKey,
                                        contentDescription = "Password reset",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "Password Reset",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "Send password reset email to user",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
8
                                    // Send reset button
                                    if (isSendingReset) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        FilledTonalButton(
                                            onClick = {
                                                onSendPasswordReset(email)
                                            },
                                            enabled = email.isNotBlank() && !resetEmailSent,
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(
                                                if (resetEmailSent) "Sent!"
                                                else "Send Reset",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }

                                // Confirmation message
                                if (resetEmailSent) {
                                    Text(
                                        text = "✓ Reset email sent to $email",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "User will receive an email to set a new password",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isModifyMode) {
                            // For modify mode - update user info only
                            onUpdateUser(name, email, displayId)
                        } else {
                            // For add mode - validate and add new user
                            if (displayId.isBlank()) return@Button
                            if (name.isBlank()) return@Button
                            if (email.isBlank()) return@Button
                            if (password.isBlank()) return@Button
                            if (password != confirmPassword) return@Button
                            if (password.length < 6) return@Button

                            onAddUser(name, email, password, displayId)
                        }
                    },
                    enabled = if (isModifyMode) isUpdateButtonEnabled else isAddButtonEnabled
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            if (isModifyMode) "Update User"
                            else "Add ${userType.replaceFirstChar { it.uppercase() }}"
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() },
                    enabled = !isLoading && !isGeneratingId && !isSendingReset
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}