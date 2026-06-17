package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MasterPharmacy(
    val id: String? = null,
    val name: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val fax: String? = null,
    val hpid: String? = null,
    val created_at: String? = null
)

fun MasterPharmacy.toPharmacy(): Pharmacy {
    return Pharmacy(
        id = this.id,
        patient_id = "",
        pharmacy_name = this.name,
        address = this.address ?: "",
        latitude = this.latitude,
        longitude = this.longitude,
        phone = this.phone ?: "",
        fax = this.fax
    )
}
