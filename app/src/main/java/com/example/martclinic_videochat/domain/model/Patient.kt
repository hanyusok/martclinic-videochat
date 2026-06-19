package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Patient(
    val id: String? = null,
    val user_id: String,
    val name: String? = null,
    val phone: String? = null,
    val resident_number: String? = null,
    val relationship: String? = "본인",
    val clinic_patient_number: String? = null,
    val created_at: String? = null
)
