package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : AppointmentRepository {

    override suspend fun getAppointments(patientId: String): List<Appointment> {
        return try {
            postgrest["appointments"]
                .select { filter { eq("patient_id", patientId) } }
                .decodeList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getAppointmentsForPatients(patientIds: List<String>): List<Appointment> {
        if (patientIds.isEmpty()) return emptyList()
        return try {
            postgrest["appointments"]
                .select { filter { isIn("patient_id", patientIds) } }
                .decodeList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getAllAppointments(): List<Appointment> {
        return try {
            postgrest["appointments"]
                .select()
                .decodeList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun createAppointment(appointment: Appointment) {
        postgrest["appointments"].insert(appointment)
    }

    override suspend fun updateAppointmentStatus(id: String, status: String) {
        try {
            postgrest["appointments"].update(mapOf("status" to status)) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateAppointmentDetails(id: String, status: String, meetLink: String?, paymentAmount: Int?) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "status" to status,
                "meet_link" to meetLink,
                "payment_amount" to paymentAmount
            )
            postgrest["appointments"].update(updates) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateAppointmentPaymentAmount(id: String, paymentAmount: Int?) {
        try {
            postgrest["appointments"].update(mapOf("payment_amount" to paymentAmount)) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
