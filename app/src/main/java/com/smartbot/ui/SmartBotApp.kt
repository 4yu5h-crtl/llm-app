package com.smartbot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartbot.ui.screens.*
import com.smartbot.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBotApp(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartBot") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    selected = currentRoute == "chat",
                    onClick = { navController.navigate("chat") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Sensors, contentDescription = "Sensors") },
                    label = { Text("Sensors") },
                    selected = currentRoute == "sensors",
                    onClick = { navController.navigate("sensors") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Robot") },
                    label = { Text("Robot") },
                    selected = currentRoute == "robot",
                    onClick = { navController.navigate("robot") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel)
            }
            composable("chat") {
                ChatScreen(viewModel = viewModel)
            }
            composable("sensors") {
                SensorsScreen(viewModel = viewModel)
            }
            composable("robot") {
                RobotControlScreen(viewModel = viewModel)
            }
        }
    }
}