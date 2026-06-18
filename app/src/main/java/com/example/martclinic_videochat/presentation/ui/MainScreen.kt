package com.example.martclinic_videochat.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.martclinic_videochat.presentation.navigation.Screen
import com.example.martclinic_videochat.presentation.ui.admin.AdminDashboardScreen

data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val icon: ImageVector
)

val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute("홈", Screen.Home, Icons.Default.Home),
    TopLevelRoute("약국", Screen.Pharmacy, Icons.Default.Place),
    TopLevelRoute("기록", Screen.History, Icons.Default.List),
    TopLevelRoute("마이", Screen.MyPage, Icons.Default.Person)
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.hierarchy?.any { it.hasRoute(Screen.AdminDashboard::class) } != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                        NavigationBarItem(
                            icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                            label = { Text(topLevelRoute.name) },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                            onClick = {
                                navController.navigate(topLevelRoute.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToAdmin = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    onNavigateToBooking = {
                        navController.navigate(Screen.Booking)
                    }
                )
            }
            composable<Screen.Booking> {
                BookingScreen()
            }
            composable<Screen.Pharmacy> {
                PharmacyScreen()
            }
            composable<Screen.History> {
                HistoryScreen()
            }
            composable<Screen.MyPage> {
                MyPageScreen(
                    onNavigateToAdmin = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.AdminDashboard> {
                AdminDashboardScreen(
                    onExitAdmin = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}


