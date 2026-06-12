package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Patient

interface PatientRepository {
    suspend fun getFirstPatient(): Patient?
    suspend fun getPatientById(id: String): Patient?
}
