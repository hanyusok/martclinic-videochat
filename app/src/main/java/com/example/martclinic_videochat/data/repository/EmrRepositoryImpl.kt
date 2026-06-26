package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.EmrPatient
import com.example.martclinic_videochat.domain.model.EmrVisit
import com.example.martclinic_videochat.domain.repository.EmrRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
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
            android.util.Log.e("EmrRepository", "searchPatientsByName failed (EMR server non-responding or exception)", e)
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
            android.util.Log.e("EmrRepository", "confirmIdentity failed (EMR server non-responding or exception) for name: $name", e)
            null
        }
    }

    override suspend fun checkInPatient(cloudMtrCreate: CloudMtrCreate): Boolean {
        return try {
            android.util.Log.d("EmrRepository", "Attempting check-in: $cloudMtrCreate")
            val response = client.post("api/mtr") {
                contentType(ContentType.Application.Json)
                setBody(cloudMtrCreate)
            }
            if (response.status.isSuccess()) {
                android.util.Log.d("EmrRepository", "Check-in success: ${response.bodyAsText()}")
                true
            } else {
                val errorBody = response.bodyAsText()
                android.util.Log.e("EmrRepository", "Check-in failed: ${response.status}. Body: $errorBody")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("EmrRepository", "Check-in exception (EMR server non-responding or exception)", e)
            false
        }
    }

    override suspend fun getPatientVisits(pcode: Int): List<EmrVisit> {
        android.util.Log.d("EmrRepository", "[EMR] getPatientVisits: pcode=$pcode")
        return try {
            val result: List<EmrVisit> = client.get("api/visits/$pcode").body()
            android.util.Log.d("EmrRepository", "[EMR] getPatientVisits success: ${result.size} visits for pcode=$pcode")
            result
        } catch (e: Exception) {
            android.util.Log.e("EmrRepository", "[EMR] getPatientVisits FAILED (EMR server non-responding or exception) for pcode=$pcode", e)
            emptyList()
        }
    }

    override suspend fun getTodayConsultationCost(pcode: Int): Int? {
        android.util.Log.d("EmrRepository", "[EMR] getTodayConsultationCost: pcode=$pcode")
        val visits = try {
            getPatientVisits(pcode)
        } catch (e: Exception) {
            android.util.Log.e("EmrRepository", "[EMR] getTodayConsultationCost FAILED: getPatientVisits exception for pcode=$pcode", e)
            return null
        }
        if (visits.isEmpty()) {
            android.util.Log.w("EmrRepository", "[EMR] getTodayConsultationCost: no visits found for pcode=$pcode")
            return null
        }

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val todayVisit = visits.firstOrNull {
            it.inDate?.startsWith(today) == true &&
                    (it.selfFee ?: 0) > 0
        }
        android.util.Log.d("EmrRepository", "[EMR] getTodayConsultationCost: today=$today, matched visit inDate=${todayVisit?.inDate}, selfFee=${todayVisit?.selfFee}")

        return todayVisit?.selfFee
    }

    override suspend fun createChart(chartCreate: com.example.martclinic_videochat.data.remote.dto.CloudChartCreate): Boolean {
        return try {
            android.util.Log.d("EmrRepository", "Attempting chart create: $chartCreate")
            val response = client.post("api/charts") {
                contentType(ContentType.Application.Json)
                setBody(chartCreate)
            }
            if (response.status.isSuccess()) {
                android.util.Log.d("EmrRepository", "Chart create success: ${response.bodyAsText()}")
                true
            } else {
                android.util.Log.e("EmrRepository", "Chart create failed: ${response.status}. Body: ${response.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("EmrRepository", "Chart create exception", e)
            false
        }
    }
}
