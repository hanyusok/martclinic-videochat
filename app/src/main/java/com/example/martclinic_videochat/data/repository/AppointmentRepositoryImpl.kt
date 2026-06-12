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

    override suspend fun createAppointment(appointment: Appointment) {
        postgrest["appointments"].insert(appointment)
    }
}
