package com.example.martclinic_videochat.data.repository

import android.util.Log
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
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

    companion object {
        private const val TAG = "AppointmentRepo"
    }
}
