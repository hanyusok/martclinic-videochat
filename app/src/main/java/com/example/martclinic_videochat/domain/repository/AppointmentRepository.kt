package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    suspend fun getAppointments(patientId: String): List<Appointment>
    suspend fun createAppointment(appointment: Appointment)
    fun observeAppointment(appointmentId: String): Flow<Appointment>
}
