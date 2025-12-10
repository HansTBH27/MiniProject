package com.example.miniproject.loginSignup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.user.UserType
import com.example.miniproject.user.UserTypeManager
import com.example.miniproject.R

@Composable
fun LoginScreen(navController: NavController? = null) {
    val primaryColor = Color(0xFF5553DC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 登录门户部分
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Login Portal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }

        // Uniserve Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Uniserve Logo",
                modifier = Modifier.size(120.dp)
            )
        }

        // Logo下方的欢迎文本
        Text(
            text = "Welcome to UniServe",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // 副标题文本
        Text(
            text = "Log in to book your facility",
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // 用户类型选择卡片
        val userTypes = listOf(
            UserTypeCardData("Student", primaryColor, R.drawable.student,
                "Access student facilities and bookings"),
            UserTypeCardData("Staff", primaryColor, R.drawable.staff,
                "Book facilities for departmental use"),
            UserTypeCardData("Admin", primaryColor, R.drawable.admin,
                "Manage all facilities and users")
        )

        userTypes.forEach { userType ->
            UserTypeCard(
                userType = userType,
                onClick = {
                    handleUserTypeSelection(userType.name, navController)
                }
            )
        }

        // 底部添加额外空间
        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class UserTypeCardData(
    val name: String,
    val color: Color,
    val iconRes: Int,
    val description: String
)

/**
 * 处理用户类型选择的函数
 */
private fun handleUserTypeSelection(
    userTypeName: String,
    navController: NavController?
) {
    // 根据选择的用户类型字符串转换为对应的枚举值
    val userType = when (userTypeName) {
        "Student" -> UserType.STUDENT
        "Staff" -> UserType.STAFF
        "Admin" -> UserType.ADMIN
        else -> null
    }

    userType?.let {
        // 设置当前用户类型
        UserTypeManager.setUserType(it)

        // 根据用户类型导航到对应的登录页面
        navController?.let { nav ->
            val route = when (it) {
                UserType.STUDENT -> "studentLogin"
                UserType.STAFF -> "staffLogin"
                UserType.ADMIN -> "adminLogin"
            }
            nav.navigate(route)
        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = userType.color
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(userType.color, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = userType.iconRes),
                        contentDescription = "${userType.name} Icon",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 标题和描述
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userType.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = userType.color
                    )
                    Text(
                        text = userType.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // 箭头图标
                Image(
                    painter = painterResource(id = R.drawable.arrowright),
                    contentDescription = "Go to ${userType.name} login",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}