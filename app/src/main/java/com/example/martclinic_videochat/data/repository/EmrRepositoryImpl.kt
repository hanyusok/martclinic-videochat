package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.EmrPatient
import com.example.martclinic_videochat.domain.repository.EmrRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class EmrRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : EmrRepository {

    override suspend fun searchPatientsByName(name: String): List<EmrPatient> {
        return try {
            val results: List<EmrPatient> = client.get("api/patients") {
                parameter("pname", name)
                parameter("limit", 50)
            }.body()
            
            // Filter out records that are missing essential identification data to avoid UI issues
            results.filter { it.name != null && it.resident_number != null }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun confirmIdentity(name: String, residentNumber: String): EmrPatient? {
        return try {
            val results: List<EmrPatient> = client.get("api/patients") {
                parameter("pname", name)
                parameter("limit", 50)
            }.body()
            
            // Normalizing resident numbers (removing hyphens) for more robust comparison
            val normalizedTargetResident = residentNumber.replace("-", "")
            
            results.find { 
                it.name == name && it.resident_number?.replace("-", "") == normalizedTargetResident 
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
