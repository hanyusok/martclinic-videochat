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
import com.example.martclinic_videochat.presentation.ui.admin.AdminMyPageScreen
import com.example.martclinic_videochat.presentation.ui.admin.AdminPharmacyScreen
import com.example.martclinic_videochat.presentation.ui.admin.AdminQueueScreen
import com.example.martclinic_videochat.presentation.ui.admin.AdminUsersScreen

data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val icon: ImageVector
)

val PATIENT_TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute("홈", Screen.Home, Icons.Default.Home),
    TopLevelRoute("약국", Screen.Pharmacy, Icons.Default.Place),
    TopLevelRoute("기록", Screen.History, Icons.Default.List),
    TopLevelRoute("마이", Screen.MyPage, Icons.Default.Person)
)

val ADMIN_TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute("대기열", Screen.AdminQueue, Icons.Default.List),
    TopLevelRoute("사용자", Screen.AdminUsers, Icons.Default.People),
    TopLevelRoute("약국 관리", Screen.AdminPharmacy, Icons.Default.Place),
    TopLevelRoute("마이", Screen.AdminMyPage, Icons.Default.Person)
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isAdminScreen = currentDestination?.hierarchy?.any {
        it.hasRoute(Screen.AdminQueue::class) ||
        it.hasRoute(Screen.AdminUsers::class) ||
        it.hasRoute(Screen.AdminPharmacy::class) ||
        it.hasRoute(Screen.AdminMyPage::class) ||
        it.hasRoute(Screen.AdminDashboard::class)
    } == true

    // Only hide bottom bar on Booking screen
    val showBottomBar = currentDestination?.hierarchy?.any { it.hasRoute(Screen.Booking::class) } != true

    val topLevelRoutes = if (isAdminScreen) ADMIN_TOP_LEVEL_ROUTES else PATIENT_TOP_LEVEL_ROUTES

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelRoutes.forEach { topLevelRoute ->
                        NavigationBarItem(
                            icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                            label = { Text(topLevelRoute.name) },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                            onClick = {
                                navController.navigate(topLevelRoute.route) {
                                    // Use type-safe route for popUpTo instead of ID
                                    popUpTo(Screen.Home) {
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
                        navController.navigate(Screen.AdminQueue) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    onNavigateToBooking = {
                        navController.navigate(Screen.Booking)
                    },
                    onNavigateToMyPage = {
                        navController.navigate(Screen.MyPage)
                    }
                )
            }
            composable<Screen.Booking> {
                BookingScreen(
                    onBack = { navController.popBackStack() }
                )
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
                        navController.navigate(Screen.AdminQueue) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
            }
            
            // --- Admin Screens ---
            composable<Screen.AdminQueue> {
                AdminQueueScreen()
            }
            composable<Screen.AdminUsers> {
                AdminUsersScreen()
            }
            composable<Screen.AdminPharmacy> {
                AdminPharmacyScreen()
            }
            composable<Screen.AdminMyPage> {
                AdminMyPageScreen(
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
