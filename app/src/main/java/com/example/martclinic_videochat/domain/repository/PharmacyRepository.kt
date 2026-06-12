package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Pharmacy

interface PharmacyRepository {
    suspend fun getAllPharmacies(): List<Pharmacy>
    suspend fun setPharmacyDefault(pharmacyId: String, patientId: String, isDefault: Boolean): Boolean
}
