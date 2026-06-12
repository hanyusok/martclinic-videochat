package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Patient(
    val id: String? = null,
    val user_id: String,
    val name: String,
    val phone: String,
    val resident_last7: String,
    val clinic_patient_number: String? = null,
    val created_at: String? = null
)
