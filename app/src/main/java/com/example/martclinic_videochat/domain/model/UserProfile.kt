package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String?,
    val role: UserRole = UserRole.PATIENT,
    val is_profile_completed: Boolean = false,
    val updated_at: String? = null
)
