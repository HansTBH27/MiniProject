package com.example.miniproject.loginSignup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showSecurityKey by remember { mutableStateOf(false) }
    var securityKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // 顶部返回按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrowleft),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "Admin Login",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "SFBS Logo",
                modifier = Modifier.size(100.dp)
            )
        }

        // 标题
        Text(
            text = "Administrator Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // 副标题
        Text(
            text = "Access the admin dashboard",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // 安全警告
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF8E1)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.security),
                    contentDescription = "Security",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Restricted access. Authorized personnel only.",
                    fontSize = 14.sp,
                    color = Color(0xFFF57C00)
                )
            }
        }

        // 错误消息显示
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.error),
                        contentDescription = "Error",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        errorMessage,
                        fontSize = 12.sp,
                        color = if (errorMessage.contains("To find") || errorMessage.contains("Firebase")) Color(0xFF2196F3) else Color(0xFFD32F2F),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 表单
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email or ID input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = { Text("Email or Admin ID") },
                placeholder = { Text("admin@university.edu or Admin ID") },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.mail),
                        contentDescription = "Email or ID",
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 密码输入
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Password",
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Image(
                            painter = painterResource(id = if (showPassword) R.drawable.eyeoff else R.drawable.eyeon),
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 安全密钥
            if (showSecurityKey) {
                OutlinedTextField(
                    value = securityKey,
                    onValueChange = { securityKey = it; errorMessage = "" },
                    label = { Text("Security Key") },
                    placeholder = { Text("Enter admin security key") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.key),
                            contentDescription = "Security Key",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 忘记密码
            TextButton(
                onClick = { /* 处理忘记密码 */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = primaryColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 登录按钮 - 修复版本
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please fill in all fields"
                    } else {
                        isLoading = true
                        errorMessage = ""

                        // Use Firebase Authentication - supports both email and ID
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val authRepository = com.example.miniproject.auth.AuthRepository()
                                val input = email.trim()
                                
                                // Detect if input is email (contains @) or ID
                                val authResult = if (input.contains("@")) {
                                    // It's an email
                                    authRepository.signIn(input, password)
                                } else {
                                    // It's an ID (displayId)
                                    authRepository.signInByDisplayId(input, password)
                                }
                                
                                // Get user data to verify role
                                authResult.user?.uid?.let { uid ->
                                    val userData = authRepository.getUserData(uid)
                                    if (userData != null && userData.role == "admin") {
                                        // Login successful - navigate to admin screen
                                        navController.navigate("admin") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = "This account is not authorized for admin login"
                                        isLoading = false
                                    }
                                } ?: run {
                                    errorMessage = "Login failed. Please check your credentials."
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "Login failed: ${e.message ?: "Invalid credentials"}"
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
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Login as Admin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Admin ID - Help Button
            TextButton(
                onClick = {
                    // Show dialog with instructions
                    CoroutineScope(Dispatchers.Main).launch {
                        errorMessage = "To find your admin ID:\n1. Check Firebase Console > Firestore > 'user' collection\n2. Look for document with role='admin'\n3. Use the 'displayId' or 'email' field to login\n\nOr create new admin via Firebase Console"
                    }
                }
            ) {
                Text(
                    "Forgot Admin ID?",
                    color = primaryColor,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 安全密钥切换
            TextButton(
                onClick = { showSecurityKey = !showSecurityKey }
            ) {
                Text(
                    if (showSecurityKey) "Hide Security Key" else "Need Security Key?",
                    color = Color.Gray
                )
            }
            
            // TEMPORARY: Direct Admin Access Button (Remove in production)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    // Direct navigation to admin screen (TEMPORARY - for development only)
                    navController.navigate("admin") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF9800) // Orange to indicate this is temporary
                )
            ) {
                Text(
                    "⚠️ Direct Admin Access (Dev Only)",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}