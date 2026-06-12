package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Pharmacy

interface PharmacyRepository {
    suspend fun getPharmaciesByPatient(patientId: String): List<Pharmacy>
    suspend fun setPharmacyDefault(pharmacyId: String, patientId: String, isDefault: Boolean): Boolean
}
