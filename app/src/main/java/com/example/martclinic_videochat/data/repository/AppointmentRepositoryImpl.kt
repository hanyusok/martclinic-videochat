package com.example.martclinic_videochat.data.repository

import android.util.Log
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@Serializable
private data class GenerateMeetLinkRequest(
    val appointment_id: String
)

@Serializable
private data class GenerateMeetLinkResponse(
    val meet_link: String
)

class AppointmentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val functions: Functions
) : AppointmentRepository {

    override suspend fun getAppointments(patientId: String): List<Appointment> {
        Log.d(TAG, "[Supabase] getAppointments: patient_id=$patientId")
        return try {
            val result = postgrest["appointments"]
                .select { filter { eq("patient_id", patientId) } }
                .decodeList<Appointment>()
            Log.d(TAG, "[Supabase] getAppointments success: ${result.size} rows")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getAppointments FAILED for patient_id=$patientId", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getAppointmentsForPatients(patientIds: List<String>): List<Appointment> {
        if (patientIds.isEmpty()) return emptyList()
        Log.d(TAG, "[Supabase] getAppointmentsForPatients: patient_ids=$patientIds")
        return try {
            val result = postgrest["appointments"]
                .select { filter { isIn("patient_id", patientIds) } }
                .decodeList<Appointment>()
            Log.d(TAG, "[Supabase] getAppointmentsForPatients success: ${result.size} rows")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getAppointmentsForPatients FAILED for patient_ids=$patientIds", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getAllAppointments(): List<Appointment> {
        Log.d(TAG, "[Supabase] getAllAppointments")
        return try {
            val result = postgrest["appointments"]
                .select()
                .decodeList<Appointment>()
            Log.d(TAG, "[Supabase] getAllAppointments success: ${result.size} rows")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getAllAppointments FAILED", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun createAppointment(appointment: Appointment) {
        Log.d(TAG, "[Supabase] createAppointment: patient_id=${appointment.patient_id}, status=${appointment.status}, symptoms=${appointment.symptoms}")
        try {
            postgrest["appointments"].insert(appointment)
            Log.d(TAG, "[Supabase] createAppointment success")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] createAppointment INSERT FAILED for patient_id=${appointment.patient_id}", e)
            throw e
        }
    }

    override suspend fun updateAppointmentStatus(id: String, status: String) {
        Log.d(TAG, "[Supabase] updateAppointmentStatus: id=$id, status=$status")
        try {
            postgrest["appointments"].update(mapOf("status" to status)) {
                filter { eq("id", id) }
            }
            Log.d(TAG, "[Supabase] updateAppointmentStatus success")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] updateAppointmentStatus UPDATE FAILED for id=$id, status=$status", e)
            e.printStackTrace()
        }
    }

    override suspend fun updateAppointmentDetails(id: String, status: String, meetLink: String?, paymentAmount: Int?) {
        Log.d(TAG, "[Supabase] updateAppointmentDetails: id=$id, status=$status, meetLink=$meetLink, paymentAmount=$paymentAmount")
        try {
            val updates = mutableMapOf<String, Any?>(
                "status" to status,
                "meet_link" to meetLink,
                "payment_amount" to paymentAmount
            )
            postgrest["appointments"].update(updates) {
                filter { eq("id", id) }
            }
            Log.d(TAG, "[Supabase] updateAppointmentDetails success")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] updateAppointmentDetails UPDATE FAILED for id=$id", e)
            e.printStackTrace()
        }
    }

    override suspend fun updateAppointmentPaymentAmount(id: String, paymentAmount: Int?) {
        Log.d(TAG, "[Supabase] updateAppointmentPaymentAmount: id=$id, paymentAmount=$paymentAmount")
        try {
            postgrest["appointments"].update(mapOf("payment_amount" to paymentAmount)) {
                filter { eq("id", id) }
            }
            Log.d(TAG, "[Supabase] updateAppointmentPaymentAmount success")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] updateAppointmentPaymentAmount UPDATE FAILED for id=$id", e)
            e.printStackTrace()
        }
    }

    override suspend fun getQueuePosition(appointmentId: String): Int? {
        Log.d(TAG, "[Supabase] getQueuePosition: appointmentId=$appointmentId")
        return try {
            val response = postgrest.rpc(
                "get_active_queue_position",
                buildJsonObject {
                    put("target_id", appointmentId)
                }
            )
            val pos = response.decodeSingleOrNull<Int>()
            Log.d(TAG, "[Supabase] getQueuePosition success: $pos")
            pos
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getQueuePosition FAILED for appointmentId=$appointmentId", e)
            e.printStackTrace()
            null
        }
    }

    override suspend fun generateMeetLink(appointmentId: String): String {
        Log.d(TAG, "[Supabase] generateMeetLink: appointmentId=$appointmentId")
        return try {
            val response = functions.invoke("generate-meet-link") {
                method = io.ktor.http.HttpMethod.Post
                setBody(GenerateMeetLinkRequest(appointmentId))
            }
            val responseText = response.bodyAsText()
            val responseData = Json.decodeFromString<GenerateMeetLinkResponse>(responseText)
            Log.d(TAG, "[Supabase] generateMeetLink success: ${responseData.meet_link}")
            responseData.meet_link
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] generateMeetLink FAILED for appointmentId=$appointmentId", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "AppointmentRepo"
    }
}
