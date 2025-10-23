package com.example.backend_read

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.backend_read.ui.dashboard.FormDashboardScreen
import com.example.backend_read.ui.dashboard.FormDashboardViewModel
import com.example.backend_read.ui.dashboard.ViewModelFactory
import com.example.backend_read.ui.login.LoginScreen
import com.example.backend_read.ui.login.LoginViewModel
import com.example.backend_read.ui.theme.Backend_ReadTheme

class MainActivity : ComponentActivity() {

    private val factory by lazy { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Backend_ReadTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        val loginViewModel = ViewModelProvider(this@MainActivity, factory)[LoginViewModel::class.java]
                        LoginScreen(viewModel = loginViewModel) {
                            // Navigate to dashboard on success, clearing the back stack.
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    composable("dashboard") {
                        val dashboardViewModel = ViewModelProvider(this@MainActivity, factory)[FormDashboardViewModel::class.java]
                        FormDashboardScreen(viewModel = dashboardViewModel)
                    }
                }
            }
        }
    }
}
