package com.example.miniproject.user

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import com.example.miniproject.user.screens.UserHomeScreen
import com.example.miniproject.user.screens.UserBookingScreen
import com.example.miniproject.user.screens.UserSettingsScreen

@Composable
fun MainScreen(navController: NavController) {
    var currentScreen by remember { mutableStateOf("Home") }
    
    // Expose function to change screen from child screens
    val onNavigateToBooking = remember {
        { currentScreen = "Booking" }
    }

    val screens = listOf("Home", "Booking", "Settings")

    Scaffold(
        bottomBar = {
            MainBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenChange = { screen ->
                    currentScreen = screen
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .pointerInput(currentScreen) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Handle swipe completion if needed
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val currentIndex = screens.indexOf(currentScreen).coerceIn(0, screens.size - 1)
                        if (kotlin.math.abs(dragAmount) > 100) { // Threshold for swipe
                            val newIndex = if (dragAmount < 0) {
                                // Swipe left - go to next screen
                                (currentIndex + 1).coerceAtMost(screens.size - 1)
                            } else {
                                // Swipe right - go to previous screen
                                (currentIndex - 1).coerceAtLeast(0)
                            }
                            if (newIndex != currentIndex) {
                                currentScreen = screens[newIndex]
                            }
                        }
                    }
                }
        ) {
            when (currentScreen) {
                "Home" -> UserHomeScreen(
                    navController = navController,
                    onNavigateToBooking = onNavigateToBooking
                )
                "Booking" -> UserBookingScreen(navController = navController)
                "Settings" -> UserSettingsScreen(navController = navController)
            }
        }
    }
}

