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
}
