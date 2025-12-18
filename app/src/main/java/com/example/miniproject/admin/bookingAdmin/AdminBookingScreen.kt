package com.example.miniproject.admin.bookingAdmin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingScreen(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        DashboardItemData(
            title = "By User",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_user"
        ),
        DashboardItemData(
            title = "By Facility",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_facility"
        ),
        DashboardItemData(
            title = "By Reservation ID",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_reservation_id"
        ),
        DashboardItemData(
            title = "By Date",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_date"
        )
    )

    // Use Scaffold only for FAB, remove the topBar
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("addEditReservation")
                },
                containerColor = Color(0xFF483D8B),
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Reservation")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        // Only show the Dashboard, which already has its own top bar
        Dashboard(
            title = "Bookings Management", // Change this title as needed
            items = items,
            onItemClick = { item -> navController.navigate(item.destinationRoute) },
            onBackClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}