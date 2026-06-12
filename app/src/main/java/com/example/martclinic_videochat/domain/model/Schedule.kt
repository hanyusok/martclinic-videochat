package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val id: String? = null,
    val doctor_id: String? = null,
    val date: String,
    val start_time: String,
    val end_time: String,
    val is_available: Boolean = true,
    val booked_by: String? = null,
    val created_at: String? = null
)
