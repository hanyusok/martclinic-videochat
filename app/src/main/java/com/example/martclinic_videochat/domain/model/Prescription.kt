package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Prescription(
    val id: String? = null,
    val appointment_id: String,
    val doctor_notes: String,
    val pdf_url: String? = null,
    val sent_pharmacy_id: String? = null,
    val sent_at: String? = null,
    val created_at: String? = null
)
