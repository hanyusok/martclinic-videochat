package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmrPatient(
    @SerialName("pname")
    val name: String? = null,
    @SerialName("pidnum_decrypted")
    val resident_number: String? = null,
    val phone: String? = null,
    @SerialName("pcode")
    val emr_patient_number: Int? = null,
    @SerialName("pbirth")
    val birth_date: String? = null,
    @SerialName("sex")
    val sex: String? = null
)
