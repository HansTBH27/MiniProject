package com.example.miniproject

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniproject.admin.AdminScreen
import com.example.miniproject.admin.bookingAdmin.AdminBookingScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByFacilityScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByReservationIdScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByUserScreen
import com.example.miniproject.admin.userAdmin.AdminStaffScreen
import com.example.miniproject.admin.userAdmin.AdminStudentScreen
import com.example.miniproject.admin.userAdmin.AdminUserScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "admin",
        modifier = modifier // Apply the modifier passed from the Scaffold
    ) {
        composable("admin") {
            AdminScreen(navController = navController)
        }

        composable("user_management") { 
            AdminUserScreen(navController = navController)
        }

        composable("student_list") {
            AdminStudentScreen(navController = navController)
        }

        composable("staff_list") {
            AdminStaffScreen(navController = navController)
        }

        composable("booking_management") {
            AdminBookingScreen(navController = navController)
        }

        composable("search_booking_by_user") {
            SearchBookingByUserScreen(navController = navController)
        }

        composable("search_booking_by_facility") {
            SearchBookingByFacilityScreen(navController = navController)
        }

        composable("search_booking_by_reservation_id") {
            SearchBookingByReservationIdScreen(navController = navController)
        }

        // TODO: Add other navigation destinations here
        // composable("search_booking_by_date") { ... }
    }
}
