package com.example.quickplan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quickplan.ui.screens.HomeScreen
import com.example.quickplan.ui.screens.AIScreen
import com.example.quickplan.ui.screens.ProfileScreen
import com.example.quickplan.ui.screens.LoginScreen
import com.example.quickplan.ui.screens.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.AI.route) { AIScreen() }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}
