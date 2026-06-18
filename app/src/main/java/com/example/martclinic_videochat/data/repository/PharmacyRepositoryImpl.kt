package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.MasterPharmacy
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.model.toPharmacy
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class PharmacyRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : PharmacyRepository {

    override suspend fun getAllPharmacies(): List<Pharmacy> {
        return try {
            val list = postgrest["favorite_pharmacies"]
                .select {
                    order("pharmacy_name", Order.ASCENDING)
                }
                .decodeList<Pharmacy>()
            
            // Filter to ensure no duplicates in the local UI list
            list.distinctBy { it.pharmacy_name + it.address }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFavoritePharmaciesForPatient(patientId: String): List<Pharmacy> {
        return try {
            val list = postgrest["favorite_pharmacies"]
                .select {
                    filter { eq("patient_id", patientId) }
                    order("pharmacy_name", Order.ASCENDING)
                }
                .decodeList<Pharmacy>()
            list.distinctBy { it.pharmacy_name + it.address }
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

    override suspend fun getNearbyPharmacies(lat: Double, lon: Double, radius: Double): List<Pharmacy> {
        return try {
            // Using RPC for spatial search via PostGIS
            val response = postgrest.rpc(
                "get_nearby_pharmacies",
                buildJsonObject {
                    put("user_lat", lat)
                    put("user_lon", lon)
                    put("radius_meters", radius)
                }
            )
            val masterList = response.decodeList<MasterPharmacy>()
            masterList.map { it.toPharmacy() }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to basic filter if RPC fails
            val degreeRange = (radius + 1000) / 111000.0
            val fallback = postgrest["pharmacies"]
                .select {
                    filter {
                        gte("latitude", lat - degreeRange)
                        lte("latitude", lat + degreeRange)
                        gte("longitude", lon - degreeRange)
                        lte("longitude", lon + degreeRange)
                    }
                    limit(50)
                }
                .decodeList<MasterPharmacy>()
            
            fallback.map { it.toPharmacy() }
        }
    }

    override suspend fun addFavoritePharmacy(patientId: String, pharmacy: Pharmacy): Boolean {
        return try {
            val favPharmacy = buildJsonObject {
                put("patient_id", patientId)
                put("pharmacy_name", pharmacy.pharmacy_name)
                put("address", pharmacy.address)
                put("latitude", pharmacy.latitude)
                put("longitude", pharmacy.longitude)
                put("phone", pharmacy.phone)
                if (pharmacy.fax != null) {
                    put("fax", pharmacy.fax)
                }
                put("is_default", pharmacy.is_default)
            }
            postgrest["favorite_pharmacies"].insert(favPharmacy)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun removeFavoritePharmacy(pharmacyId: String): Boolean {
        return try {
            postgrest["favorite_pharmacies"].delete {
                filter { eq("id", pharmacyId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updatePharmacyFax(pharmacyId: String, fax: String): Boolean {
        return try {
            // 1. First, find the pharmacy by ID to get its name and address (our unique keys)
            // We search both tables because we don't know which ID was passed
            val masterInfo = try {
                postgrest["pharmacies"].select {
                    filter { eq("id", pharmacyId) }
                }.decodeSingle<MasterPharmacy>()
            } catch (e: Exception) {
                val favorite = postgrest["favorite_pharmacies"].select {
                    filter { eq("id", pharmacyId) }
                }.decodeSingle<Pharmacy>()
                MasterPharmacy(name = favorite.pharmacy_name, address = favorite.address, latitude = favorite.latitude, longitude = favorite.longitude)
            }

            // 2. Update the master pharmacies table (Shared by everyone)
            postgrest["pharmacies"].update(mapOf("fax" to fax)) {
                filter {
                    eq("name", masterInfo.name)
                    eq("address", masterInfo.address ?: "")
                }
            }

            // 3. Update all favorite entries matching this pharmacy to sync for ALL users
            postgrest["favorite_pharmacies"].update(mapOf("fax" to fax)) {
                filter {
                    eq("pharmacy_name", masterInfo.name)
                    eq("address", masterInfo.address ?: "")
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getMasterPharmacies(): List<Pharmacy> {
        return try {
            val list = postgrest["pharmacies"]
                .select {
                    limit(50)
                }
                .decodeList<MasterPharmacy>()
            list.map { it.toPharmacy() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
