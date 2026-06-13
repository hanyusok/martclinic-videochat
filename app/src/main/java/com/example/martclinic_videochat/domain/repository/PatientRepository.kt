package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Patient

interface PatientRepository {
    suspend fun getPatients(): List<Patient>
    suspend fun getFirstPatient(): Patient?
    suspend fun createPatient(patient: Patient): Boolean
    suspend fun updatePatient(patient: Patient): Boolean
    suspend fun deletePatient(patientId: String): Boolean
}
