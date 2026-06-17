package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Pharmacy(
    val id: String? = null,
    val patient_id: String,
    val pharmacy_name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val fax: String? = null,
    val is_default: Boolean = false,
    val created_at: String? = null
)
