package com.example.quickplan.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import com.example.quickplan.navigation.Screen

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.AI, Screen.Profile)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {},
                label = { Text(screen.title) }
            )
        }
    }
}
