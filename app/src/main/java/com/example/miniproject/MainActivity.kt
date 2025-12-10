package com.example.miniproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miniproject.admin.AdminScreen
import com.example.miniproject.loginSignup.LoginScreen
import com.example.miniproject.loginSignup.StudentLoginScreen
import com.example.miniproject.loginSignup.StaffLoginScreen
import com.example.miniproject.loginSignup.AdminLoginScreen
import com.example.miniproject.loginSignup.SignUpScreen
import com.example.miniproject.ui.theme.HomeScreen
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniServeAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    @Composable
    fun AppNavigation(modifier: Modifier) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") {
                LoginScreen(navController = navController)
            }

            // 学生登录
            composable("studentLogin") {
                StudentLoginScreen(navController = navController)
            }

            // 员工登录
            composable("staffLogin") {
                StaffLoginScreen(navController = navController)
            }

            // 管理员登录
            composable("adminLogin") {
                AdminLoginScreen(navController = navController)
            }

            // 注册页面（带用户类型参数）
            composable(
                route = "signup/{userType}",
                arguments = listOf(
                    navArgument("userType") {
                        type = NavType.StringType
                        defaultValue = "Student"
                    }
                )
            ) { backStackEntry ->
                val userType = backStackEntry.arguments?.getString("userType") ?: "Student"
                SignUpScreen(
                    navController = navController,
                    userType = userType
                )
            }

            composable("home") {
                HomeScreen(navController = navController)
            }

            composable("admin") {
                AdminScreen(navController = navController)
            }
        }
    }

    @Composable
    fun UniServeAppTheme(content: @Composable () -> Unit) {
        val primaryColor = Color(0xFF5553DC)
        androidx.compose.material3.MaterialTheme(
            colorScheme = androidx.compose.material3.lightColorScheme(
                primary = primaryColor,
                secondary = primaryColor,
                tertiary = primaryColor
            ),
            content = content
        )
    }
}