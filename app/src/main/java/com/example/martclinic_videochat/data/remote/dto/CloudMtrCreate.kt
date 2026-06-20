package com.example.martclinic_videochat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CloudMtrCreate(
    val pcode: Int? = null,
    val pname: String? = null,
    val pbirth: String? = null
)
