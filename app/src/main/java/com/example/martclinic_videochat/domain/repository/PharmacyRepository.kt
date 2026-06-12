package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Pharmacy

interface PharmacyRepository {
    suspend fun getAllPharmacies(): List<Pharmacy>
    suspend fun getPharmaciesByPatient(patientId: String): List<Pharmacy>
    suspend fun getDefaultPharmacy(patientId: String): Pharmacy?
    suspend fun getPharmacyById(id: String): Pharmacy?
    suspend fun setPharmacyDefault(pharmacyId: String, patientId: String, isDefault: Boolean): Boolean
}
