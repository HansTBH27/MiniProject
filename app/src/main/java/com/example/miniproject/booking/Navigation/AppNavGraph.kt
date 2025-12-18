package com.example.miniproject.booking.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Import your screens
import com.example.miniproject.booking.UI.FacilityListScreen
import com.example.miniproject.booking.UI.FacilityDetailsScreen
import com.example.miniproject.booking.UI.BookingFormScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.BookingHistory.route
    ) {
        composable("booking_history/{username}/{userId}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            BookingHistoryScreen(
                navController = navController,
                username = username,
                userId = userId
            )
        }

        composable(AppRoute.FacilityList.route) {
            FacilityListScreen(
                onViewDetails = { id ->
                    navController.navigate(AppRoute.FacilityDetails.createRoute(id))
                },
                onBookNow = { id ->
                    navController.navigate(AppRoute.BookingForm.createRoute(id))
                }
            )
        }

        composable(AppRoute.FacilityDetails.route) { backStackEntry ->
            val facilityId = backStackEntry.arguments?.getString("facilityId") ?: ""
            FacilityDetailsScreen(
                facilityId = facilityId,
                onBookNow = {
                    navController.navigate(AppRoute.BookingForm.createRoute(facilityId))
                }
            )
        }

        composable(AppRoute.BookingForm.route) { backStackEntry ->
            val facilityId = backStackEntry.arguments?.getString("facilityId") ?: ""
            BookingFormScreen(facilityId = facilityId)
        }
    }
}