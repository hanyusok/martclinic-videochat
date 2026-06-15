package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.data.model.ExternalPharmacyResponse
import com.example.martclinic_videochat.domain.model.MasterPharmacy
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class PharmacyRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val client: HttpClient
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

    override suspend fun fetchAndStoreNearbyPharmacies(lat: Double, lon: Double): Result<Unit> {
        return try {
            // 1. Fetch from external API
            val externalPharmacies: List<ExternalPharmacyResponse> = client.get("api/pharmacies") {
                parameter("lat", lat)
                parameter("lng", lon) // Using common 'lng' parameter
                parameter("lon", lon) // Keeping 'lon' for compatibility
                parameter("radius", 10000) // 10km radius
                parameter("limit", 100)
            }.body()

            // 2. Map to MasterPharmacy and insert into Supabase
            val masterPharmacies = externalPharmacies.map {
                MasterPharmacy(
                    name = it.name,
                    address = it.address,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    phone = it.phone
                )
            }

            if (masterPharmacies.isNotEmpty()) {
                // Using the lambda configuration for upsert in supabase-kt
                postgrest["pharmacies"].upsert(masterPharmacies) {
                    onConflict = "name,address"
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getNearbyPharmacies(lat: Double, lon: Double, radius: Double): List<Pharmacy> {
        return try {
            // Increase range slightly to be safe (roughly 0.01 degree ~ 1.1km)
            val degreeRange = (radius + 1000) / 111000.0
            
            val masterList = postgrest["pharmacies"]
                .select {
                    filter {
                        gte("latitude", lat - degreeRange)
                        lte("latitude", lat + degreeRange)
                        gte("longitude", lon - degreeRange)
                        lte("longitude", lon + degreeRange)
                    }
                    limit(50) // Limit results for performance
                }
                .decodeList<MasterPharmacy>()
            
            // Map MasterPharmacy to Pharmacy (without patient_id for now)
            masterList.map {
                Pharmacy(
                    id = it.id,
                    patient_id = "", // Placeholder
                    pharmacy_name = it.name,
                    address = it.address ?: "",
                    latitude = it.latitude,
                    longitude = it.longitude,
                    phone = it.phone ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun addFavoritePharmacy(patientId: String, pharmacy: Pharmacy): Boolean {
        return try {
            postgrest["favorite_pharmacies"].insert(
                pharmacy.copy(patient_id = patientId, id = null) // Ensure patient_id is set and let DB generate id
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
