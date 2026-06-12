package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.Prescription
import com.example.martclinic_videochat.domain.repository.PrescriptionRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.Instant
import javax.inject.Inject

class PrescriptionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : PrescriptionRepository {

    override suspend fun getPrescriptionByAppointment(appointmentId: String): Prescription? {
        return try {
            val list = postgrest["prescriptions"]
                .select {
                    filter {
                        eq("appointment_id", appointmentId)
                    }
                }
                .decodeList<Prescription>()
            list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun sendPrescriptionToPharmacy(prescriptionId: String, pharmacyId: String): Boolean {
        return try {
            val nowStr = Instant.now().toString()
            postgrest["prescriptions"].update(
                mapOf(
                    "sent_pharmacy_id" to pharmacyId,
                    "sent_at" to nowStr
                )
            ) {
                filter {
                    eq("id", prescriptionId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
