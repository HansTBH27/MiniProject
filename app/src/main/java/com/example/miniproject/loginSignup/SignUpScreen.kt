package com.example.miniproject.loginSignup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun SignUpScreen(
    navController: NavController,
    userType: String = "Student"
) {
    val primaryColor = Color(0xFF5553DC)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
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
                "$userType Sign Up",
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
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Uniserve Logo",
                modifier = Modifier.size(80.dp)
            )
        }

        // 标题
        Text(
            text = "Create $userType Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // 副标题
        Text(
            text = "Fill in your details to register",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

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
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F)
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
            // 全名
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = "" },
                label = { Text("Full Name") },
                placeholder = { Text("Enter your full name") },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Name",
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 邮箱
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = { Text("Email Address") },
                placeholder = {
                    Text(
                        when (userType) {
                            "Staff" -> "staff@university.edu"
                            "Admin" -> "admin@university.edu"
                            else -> "student@university.edu"
                        }
                    )
                },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.mail),
                        contentDescription = "Email",
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 电话号码
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it; errorMessage = "" },
                label = { Text("Phone Number") },
                placeholder = { Text("+60 12-345 6789") },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.phone),
                        contentDescription = "Phone",
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 用户ID（学生/员工ID）
            val idLabel = when (userType) {
                "Student" -> "Student ID"
                "Staff" -> "Staff ID"
                else -> "User ID"
            }

            // 根据用户类型选择图标
            val idIconRes = when (userType) {
                "Student" -> R.drawable.user // 使用 studentid.png
                "Staff" -> R.drawable.user    // 假设有 staffid.png
                else -> R.drawable.user       // 默认图标
            }

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it; errorMessage = "" },
                label = { Text(idLabel) },
                placeholder = {
                    Text(
                        when (userType) {
                            "Student" -> "e.g., CB20000"
                            "Staff" -> "e.g., STF20000"
                            else -> "Enter your ID"
                        }
                    )
                },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = idIconRes),
                        contentDescription = "ID Card",
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 院系/部门选择
            val departmentLabel = when (userType) {
                "Student" -> "Program of Study"
                "Staff" -> "Department"
                else -> "Department"
            }
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = department,
                    onValueChange = {},
                    label = { Text(departmentLabel) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.department),
                            contentDescription = "Department",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.arrowdown),
                            contentDescription = "Dropdown",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 密码
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = { Text("Password") },
                placeholder = { Text("Minimum 6 characters") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 确认密码
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = "" },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Confirm Password",
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

            Spacer(modifier = Modifier.height(24.dp))

            // 条款和条件复选框
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = primaryColor,
                        uncheckedColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "I agree to the ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    "Terms & Conditions",
                    fontSize = 14.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* 显示条款 */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 注册按钮
            Button(
                onClick = {
                    // 验证表单
                    if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        errorMessage = "Please fill in all required fields"
                    } else if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                    } else if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                    } else if (!acceptedTerms) {
                        errorMessage = "Please accept the terms and conditions"
                    } else {
                        isLoading = true
                        errorMessage = ""

                        // 使用协程作用域
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                // TODO: 这里添加实际的注册逻辑（Firebase Authentication）
                                // 模拟注册过程
                                kotlinx.coroutines.delay(2000)

                                // 注册成功
                                // 导航回登录页面
                                navController.popBackStack()
                            } finally {
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
                        "Create Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 登录链接
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = Color.Gray)
                Text(
                    "Login",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 或者使用其他方式注册
            Text(
                "Or sign up with",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 社交注册按钮 - 仅保留 Google
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Google 注册 */ },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFDB4437), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}