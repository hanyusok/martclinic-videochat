package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String? = null,
    val patient_id: String,
    val schedule_id: String,
    val status: String,
    val symptoms: String,
    val meet_link: String? = null,
    val payment_amount: Int? = null,
    val created_at: String? = null
)
