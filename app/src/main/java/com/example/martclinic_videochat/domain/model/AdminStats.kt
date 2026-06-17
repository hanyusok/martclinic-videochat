package com.example.martclinic_videochat.domain.model

data class AdminStats(
    val totalPatients: Int = 0,
    val activeAppointments: Int = 0,
    val completedAppointmentsToday: Int = 0,
    val totalRevenueToday: Int = 0
)
