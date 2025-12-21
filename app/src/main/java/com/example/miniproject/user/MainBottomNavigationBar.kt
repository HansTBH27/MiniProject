package com.example.miniproject.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainBottomNavigationBar(
    currentScreen: String,
    onScreenChange: (String) -> Unit
) {
    val primaryColor = Color(0xFF5553DC) // Unified color for all users

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating bar with unified color
        Row(
            modifier = Modifier
                .width(360.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(25.dp))
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(25.dp),
                    clip = true
                )
                .background(primaryColor)
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                screen = "Home",
                currentScreen = currentScreen,
                icon = Icons.Default.Home,
                label = "Home",
                onScreenChange = onScreenChange,
                primaryColor = primaryColor
            )

            NavItem(
                screen = "Booking",
                currentScreen = currentScreen,
                icon = Icons.Default.BookOnline,
                label = "Booking",
                onScreenChange = onScreenChange,
                primaryColor = primaryColor
            )

            NavItem(
                screen = "Settings",
                currentScreen = currentScreen,
                icon = Icons.Default.Settings,
                label = "Settings",
                onScreenChange = onScreenChange,
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
private fun NavItem(
    screen: String,
    currentScreen: String,
    icon: ImageVector,
    label: String,
    onScreenChange: (String) -> Unit,
    primaryColor: Color
) {
    val isSelected = currentScreen == screen
    val iconColor = if (isSelected) Color.White else Color(0xFFCCCCCC)
    val textColor = if (isSelected) Color.White else Color(0xFFCCCCCC)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onScreenChange(screen) }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // White dot indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Text
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

