package com.example.martclinic_videochat.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Home : Screen
    
    @Serializable
    data object Booking : Screen
    
    @Serializable
    data object Pharmacy : Screen
    
    @Serializable
    data object History : Screen
    
    @Serializable
    data object MyPage : Screen

    @Serializable
    data object AdminDashboard : Screen // We'll keep this temporarily or remove it if not used anywhere else
    
    @Serializable
    data object AdminQueue : Screen

    @Serializable
    data object AdminUsers : Screen

    @Serializable
    data object AdminPharmacy : Screen

    @Serializable
    data object AdminMyPage : Screen
}
