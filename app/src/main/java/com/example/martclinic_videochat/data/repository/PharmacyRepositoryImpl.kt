package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class PharmacyRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : PharmacyRepository {

    override suspend fun getAllPharmacies(): List<Pharmacy> {
        return try {
            postgrest["favorite_pharmacies"]
                .select {
                    order("pharmacy_name", Order.ASCENDING)
                }
                .decodeList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPharmacyById(id: String): Pharmacy? {
        return try {
            val list = postgrest["favorite_pharmacies"]
                .select {
                    filter { eq("id", id) }
                }
                .decodeList<Pharmacy>()
            list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override suspend fun getDefaultPharmacy(patientId: String): Pharmacy? {
        return try {
            val list = postgrest["favorite_pharmacies"]
                .select {
                    filter {
                        eq("patient_id", patientId)
                        eq("is_default", true)
                    }
                }
                .decodeList<Pharmacy>()
            list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun setPharmacyDefault(pharmacyId: String, patientId: String, isDefault: Boolean): Boolean {
        return try {
            if (isDefault) {
                // Set all other pharmacies for this patient to non-default
                postgrest["favorite_pharmacies"].update(mapOf("is_default" to false)) {
                    filter { eq("patient_id", patientId) }
                }
                // Set target pharmacy to default (Removed redundant patient_id update)
                postgrest["favorite_pharmacies"].update(mapOf("is_default" to true)) {
                    filter { eq("id", pharmacyId) }
                }
            } else {
                postgrest["favorite_pharmacies"].update(mapOf("is_default" to false)) {
                    filter { eq("id", pharmacyId) }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
