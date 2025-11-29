package com.example.miniproject.admin

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun AdminScreen(navController: NavController, modifier: Modifier = Modifier){ // Added modifier
    // The text will now appear at the top of the padded area.
    Text(
        text = "Admin Dashboard",
        modifier = modifier
    )
}