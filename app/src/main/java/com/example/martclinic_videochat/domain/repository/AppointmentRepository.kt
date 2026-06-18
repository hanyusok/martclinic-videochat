package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Appointment

interface AppointmentRepository {
    suspend fun getAppointments(patientId: String): List<Appointment>
    suspend fun getAllAppointments(): List<Appointment>
    suspend fun createAppointment(appointment: Appointment)
    suspend fun updateAppointmentStatus(id: String, status: String)
    suspend fun updateAppointmentDetails(id: String, status: String, meetLink: String?, paymentAmount: Int?)
}
