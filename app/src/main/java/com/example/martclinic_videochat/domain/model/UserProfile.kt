package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String?,
    val role: UserRole = UserRole.PATIENT,
    val updated_at: String? = null
)
