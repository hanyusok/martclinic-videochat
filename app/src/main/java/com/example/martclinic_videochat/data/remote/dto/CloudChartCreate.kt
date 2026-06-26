package com.example.martclinic_videochat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CloudChartCreate(
    val pcode: Int,
    val symptom: String? = null,
    val doc: String? = null
)
