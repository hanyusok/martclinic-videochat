package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.MasterPharmacy
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.HttpClient
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
        // Since we are now using the full master database from pharm_db.csv,
        // we no longer need to fetch from an external API per request.
        return Result.success(Unit)
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
            
            masterList.map { item ->
                Pharmacy(
                    id = item.id,
                    patient_id = "", 
                    pharmacy_name = item.name,
                    address = item.address ?: "",
                    latitude = item.latitude,
                    longitude = item.longitude,
                    phone = item.phone ?: ""
                )
            }
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
            
            fallback.map { item ->
                Pharmacy(
                    id = item.id,
                    patient_id = "",
                    pharmacy_name = item.name,
                    address = item.address ?: "",
                    latitude = item.latitude,
                    longitude = item.longitude,
                    phone = item.phone ?: ""
                )
            }
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
