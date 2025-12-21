package com.example.miniproject.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.auth.FirebaseManager
import kotlinx.coroutines.launch

@Composable
fun UserSettingsScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    
    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Load user data
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val currentUser = FirebaseManager.auth.currentUser
                if (currentUser != null) {
                    val authRepository = AuthRepository()
                    val userData = authRepository.getUserData(currentUser.uid)
                    userName = userData?.name ?: "User"
                    userEmail = userData?.email ?: ""
                    userRole = userData?.role?.replaceFirstChar { it.uppercase() } ?: ""
                }
            } catch (e: Exception) {
                println("Error loading user data: ${e.message}")
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header without back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8))
                    .verticalScroll(rememberScrollState())
            ) {
                // User Profile Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = userRole,
                            fontSize = 12.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Account Settings
                SettingsCategory(title = "Account Settings", primaryColor = primaryColor) {
                    SettingsItem(
                        icon = R.drawable.person,
                        title = "Personal Information",
                        subtitle = "Update your personal details",
                        iconColor = primaryColor,
                        onClick = {
                            // Navigate to profile edit if needed
                        }
                    )

                    SettingsItem(
                        icon = R.drawable.lock,
                        title = "Change Password",
                        subtitle = "Update your password",
                        iconColor = primaryColor,
                        onClick = {
                            // Show change password dialog
                        }
                    )
                }

                // App Settings
                SettingsCategory(title = "App Settings", primaryColor = primaryColor) {
                    SettingsItem(
                        icon = R.drawable.mail,
                        title = "Notifications",
                        subtitle = "Manage notification preferences",
                        iconColor = primaryColor,
                        onClick = {
                            // Navigate to notifications settings
                        }
                    )
                    
                    SettingsItem(
                        icon = R.drawable.error,
                        title = "About",
                        subtitle = "App version and information",
                        iconColor = Color(0xFF757575),
                        onClick = {
                            // Show about dialog
                        }
                    )
                }

                // Support
                SettingsCategory(title = "Support", primaryColor = primaryColor) {
                    SettingsItem(
                        icon = R.drawable.phone,
                        title = "Help Center",
                        subtitle = "Get help and support",
                        iconColor = Color(0xFF757575),
                        onClick = {
                            // Navigate to help
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Danger Zone
                SettingsCategory(title = "Danger Zone", primaryColor = Color(0xFFD32F2F)) {
                    SettingsItem(
                        icon = R.drawable.security,
                        title = "Logout",
                        subtitle = "Sign out from your account",
                        iconColor = Color(0xFFD32F2F),
                        onClick = { showLogoutDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // User Type Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = primaryColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(primaryColor.copy(alpha = 0.05f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(primaryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Logged in as: $userRole",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                        }
                    }
                }

                // App Version
                Text(
                    text = "SFBS Smart Facility Booking System v1.0.0",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                FirebaseManager.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                println("Error logging out: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = primaryColor)
                }
            }
        )
    }
}

@Composable
fun SettingsCategory(
    title: String,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.arrowright),
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}
