package com.example.miniproject.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFailedScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Failed", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Error Icon
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Error Message
                Text(
                    text = "Booking Failed",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Unfortunately, your booking could not be completed. This may be due to a payment issue or system error.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Button(
                    onClick = {
                        navController.navigate("main") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Go to Home",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = {
                        navController.navigate("user_booking") {
                            popUpTo("main") { inclusive = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Try Booking Again",
                        fontSize = 16.sp,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

