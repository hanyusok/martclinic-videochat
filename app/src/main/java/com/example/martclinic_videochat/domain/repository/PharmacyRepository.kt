package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Pharmacy

interface PharmacyRepository {
    suspend fun getAllPharmacies(): List<Pharmacy>
    suspend fun getDefaultPharmacy(patientId: String): Pharmacy?
    suspend fun getPharmacyById(id: String): Pharmacy?
    suspend fun setPharmacyDefault(pharmacyId: String, patientId: String, isDefault: Boolean): Boolean
    
    // New methods for external API sync
    suspend fun fetchAndStoreNearbyPharmacies(lat: Double, lon: Double): Result<Unit>
    suspend fun getNearbyPharmacies(lat: Double, lon: Double, radius: Double): List<Pharmacy>
    suspend fun addFavoritePharmacy(patientId: String, pharmacy: Pharmacy): Boolean
}
