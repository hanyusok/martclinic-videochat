package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.EmrPatient
import com.example.martclinic_videochat.domain.repository.EmrRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import com.example.martclinic_videochat.data.remote.dto.CloudMtrCreate
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

    override suspend fun confirmIdentity(name: String, birthDate: String): EmrPatient? {
        return try {
            val results: List<EmrPatient> = client.get("api/patients") {
                parameter("pname", name)
                parameter("limit", 50)
            }.body()
            
            results.find { 
                it.name == name && it.birth_date == birthDate 
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getPatientDetail(pcode: Int): Boolean {
        return try {
            val response = client.get("api/patients/$pcode")
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun checkInPatient(cloudMtrCreate: CloudMtrCreate): Boolean {
        return try {
            val response = client.post("api/mtr") {
                contentType(ContentType.Application.Json)
                setBody(cloudMtrCreate)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
