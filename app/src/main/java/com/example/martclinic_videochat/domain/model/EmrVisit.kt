package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmrVisit(
    @SerialName("selfee") val selfFee: Int? = null,
    @SerialName("visidate") val inDate: String? = null,
    @SerialName("fin") val fin: String? = null
)
