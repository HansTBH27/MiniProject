package com.example.miniproject.loginSignup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.auth.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    userType: String = "Student"
) {
    val authRepository = AuthRepository()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var displayId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Color based on user type
    val primaryColor = Color(0xFF5553DC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "$userType Sign Up",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Icon(
            Icons.Default.PersonAdd,
            contentDescription = "Sign Up",
            modifier = Modifier.size(60.dp),
            tint = primaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create $userType Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )

        Text(
            text = "Join SFBS today",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Success message
        if (successMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF5553DC)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = successMessage,
                        color = Color(0xFF5553DC),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; errorMessage = "" },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Name")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ID field
        OutlinedTextField(
            value = displayId,
            onValueChange = { 
                // Only allow numbers for student, numbers with optional S prefix for staff
                val role = userType.lowercase()
                if (role == "student") {
                    // Student: only numbers
                    if (it.all { char -> char.isDigit() }) {
                        displayId = it
                        errorMessage = ""
                    }
                } else if (role == "staff") {
                    // Staff: S prefix + numbers, or just numbers
                    if (it.isEmpty() || (it.startsWith("S", ignoreCase = true) && it.drop(1).all { char -> char.isDigit() }) || it.all { char -> char.isDigit() }) {
                        displayId = it
                        errorMessage = ""
                    }
                } else {
                    displayId = it
                    errorMessage = ""
                }
            },
            label = { 
                Text(
                    if (userType.lowercase() == "student") "Student ID" 
                    else "Staff ID"
                ) 
            },
            placeholder = { 
                Text(
                    if (userType.lowercase() == "student") "e.g., 1001" 
                    else "e.g., S1001 or 1001"
                ) 
            },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "ID")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )
        
        // Helper text for ID format
        Text(
            text = if (userType.lowercase() == "student") 
                "Enter your student ID (numbers only)" 
            else 
                "Enter your staff ID (e.g., S1001 or 1001)",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = "" },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button
        Button(
            onClick = {
                // Validation
                val role = userType.lowercase()
                when {
                    name.isBlank() -> errorMessage = "Please enter your full name"
                    displayId.isBlank() -> errorMessage = "Please enter your ${if (role == "student") "student" else "staff"} ID"
                    email.isBlank() -> errorMessage = "Please enter your email"
                    password.isBlank() -> errorMessage = "Please enter a password"
                    confirmPassword.isBlank() -> errorMessage = "Please confirm your password"
                    password != confirmPassword -> errorMessage = "Passwords do not match"
                    password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorMessage = "Please enter a valid email address"
                    role == "student" && !displayId.all { it.isDigit() } -> errorMessage = "Student ID must contain only numbers"
                    role == "staff" -> {
                        val cleanId = if (displayId.startsWith("S", ignoreCase = true)) displayId.drop(1) else displayId
                        if (cleanId.isBlank() || !cleanId.all { it.isDigit() }) {
                            errorMessage = "Staff ID must be numbers only (with optional S prefix)"
                        } else {
                            // Valid, proceed
                            errorMessage = ""
                        }
                    }
                    else -> {
                        errorMessage = ""
                    }
                }
                
                // If no validation errors, proceed with signup
                if (errorMessage.isEmpty() && name.isNotBlank() && displayId.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                    isLoading = true
                    errorMessage = ""
                    successMessage = ""

                    coroutineScope.launch {
                        try {
                            // Use the repository to sign up with the correct role and displayId
                            val authResult = authRepository.signUp(email, password, name, role, displayId)
                            
                            successMessage = "Account created successfully! Your ID is ${if (role == "staff" && !displayId.startsWith("S", ignoreCase = true)) "S$displayId" else displayId}. Redirecting to login..."
                            isLoading = false
                            
                            // Navigate to login after a short delay
                            kotlinx.coroutines.delay(2000)
                            val loginRoute = when (role) {
                                "student" -> "studentLogin"
                                "staff" -> "staffLogin"
                                else -> "login"
                            }
                            navController.navigate(loginRoute) {
                                popUpTo("login") { inclusive = false }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Sign up failed: ${e.message ?: "Unknown error"}"
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = "Sign Up as $userType",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = Color.Gray)
            Text(
                text = "Login",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    enabled = !isLoading,
                    onClick = {
                        when (userType.lowercase()) {
                            "student" -> navController.navigate("studentLogin") {
                                popUpTo("signup/{userType}") { inclusive = true }
                            }
                            "staff" -> navController.navigate("staffLogin") {
                                popUpTo("signup/{userType}") { inclusive = true }
                            }
                            else -> navController.popBackStack()
                        }
                    }
                )
            )
        }
    }
}
