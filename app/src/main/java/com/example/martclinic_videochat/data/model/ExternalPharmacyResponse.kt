package com.example.martclinic_videochat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExternalPharmacyResponse(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val phone: String?
)
