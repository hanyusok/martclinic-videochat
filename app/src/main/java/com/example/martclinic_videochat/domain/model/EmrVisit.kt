package com.example.martclinic_videochat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmrVisit(
    @SerialName("selfee") val selfFee: Int? = null,
    @SerialName("selfee2") val selfFee2: Int? = null,
    @SerialName("visidate") val inDate: String? = null
)
