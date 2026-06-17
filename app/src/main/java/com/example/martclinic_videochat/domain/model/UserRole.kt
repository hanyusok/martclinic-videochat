package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("admin")
    ADMIN,
    @SerialName("doctor")
    DOCTOR,
    @SerialName("patient")
    PATIENT;

    override fun toString(): String = name.lowercase()
}
