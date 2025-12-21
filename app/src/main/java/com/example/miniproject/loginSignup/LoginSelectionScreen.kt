package com.example.miniproject.loginSignup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R

data class UserTypeCardData(
    val name: String,
    val color: Color,
    val iconRes: Int,
    val description: String
)

@Composable
fun LoginSelectionScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    val studentColor = primaryColor
    val staffColor = primaryColor
    val adminColor = primaryColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "SFBS Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Welcome text
        Text(
            text = "Welcome to SFBS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = studentColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your account type to continue",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // User type selection cards
        val userTypes = listOf(
            UserTypeCardData("Student", studentColor, R.drawable.student,
                "Access student facilities and bookings"),
            UserTypeCardData("Staff", staffColor, R.drawable.staff,
                "Book facilities for departmental use"),
            UserTypeCardData("Admin", adminColor, R.drawable.admin,
                "Manage all facilities and users")
        )

        userTypes.forEach { userType ->
            UserTypeCard(
                userType = userType,
                onClick = {
                    when (userType.name) {
                        "Student" -> navController.navigate("studentLogin")
                        "Staff" -> navController.navigate("staffLogin")
                        "Admin" -> navController.navigate("adminLogin")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Sign up option (only for Student and Staff)
        Text(
            text = "New to SFBS?",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Student Sign Up Button
            OutlinedButton(
                onClick = { navController.navigate("signup/Student") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = studentColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = SolidColor(studentColor)
                )
            ) {
                Text("Student Sign Up", fontWeight = FontWeight.Bold)
            }

            // Staff Sign Up Button
            OutlinedButton(
                onClick = { navController.navigate("signup/Staff") },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = staffColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = SolidColor(staffColor)
                )
            ) {
                Text("Staff Sign Up", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Note: Admin accounts can only be created by existing admins",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun UserTypeCard(
    userType: UserTypeCardData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = userType.color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(userType.color, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = userType.iconRes),
                    contentDescription = "${userType.name} Icon",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userType.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = userType.color
                )
                Text(
                    text = userType.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Arrow icon
            Text(
                "→",
                fontSize = 24.sp,
                color = userType.color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

