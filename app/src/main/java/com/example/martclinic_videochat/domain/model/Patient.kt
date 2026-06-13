package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Patient(
    val id: String? = null,
    val user_id: String,
    val name: String,
    val phone: String,
    @SerialName("resident_last7")
    val resident_number: String,
    val clinic_patient_number: String? = null,
    val created_at: String? = null
)
