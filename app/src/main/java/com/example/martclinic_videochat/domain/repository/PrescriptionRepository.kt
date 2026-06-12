package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Prescription

interface PrescriptionRepository {
    suspend fun getPrescriptionByAppointment(appointmentId: String): Prescription?
    suspend fun sendPrescriptionToPharmacy(prescriptionId: String, pharmacyId: String): Boolean
}
