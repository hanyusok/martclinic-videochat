package com.example.martclinic_videochat.domain.usecase

import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import javax.inject.Inject

class GetAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(patientId: String): List<Appointment> {
        return repository.getAppointments(patientId)
    }
}
