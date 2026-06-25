package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String? = null,
    val appointment_id: String,
    val patient_id: String,
    val transaction_id: String? = null,
    val amount: Int? = null,
    val pay_method: String? = null,
    val status: String,
    val created_at: String? = null
)
