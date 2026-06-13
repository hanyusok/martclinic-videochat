package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.EmrPatient

interface EmrRepository {
    suspend fun searchPatientsByName(name: String): List<EmrPatient>
    suspend fun confirmIdentity(name: String, residentNumber: String): EmrPatient?
}
