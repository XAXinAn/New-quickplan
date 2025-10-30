package com.example.quickplan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quickplan.ui.screens.AIScreen
import com.example.quickplan.ui.screens.AddScheduleScreen
import com.example.quickplan.ui.screens.EditScheduleScreen
import com.example.quickplan.ui.screens.HomeScreen
import com.example.quickplan.ui.screens.LoginScreen
import com.example.quickplan.ui.screens.ProfileScreen
import com.example.quickplan.ui.screens.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { backStackEntry ->
            // 从后退栈中获取可能更新的日期
            val selectedDate = backStackEntry.savedStateHandle.get<String>("selectedDate")
            HomeScreen(navController = navController, initialDate = selectedDate)
        }
        composable(Screen.AI.route) { AIScreen() }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable(
            route = "addSchedule/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            AddScheduleScreen(navController = navController, defaultDate = date)
        }
        composable(
            route = "editSchedule/{scheduleId}",
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            if (scheduleId != null) {
                EditScheduleScreen(navController = navController, scheduleId = scheduleId)
            }
        }
    }
}
