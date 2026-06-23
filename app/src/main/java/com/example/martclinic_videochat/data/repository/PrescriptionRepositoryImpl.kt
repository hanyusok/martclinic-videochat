package com.example.martclinic_videochat.data.repository

import android.util.Log
import com.example.martclinic_videochat.domain.model.Prescription
import com.example.martclinic_videochat.domain.repository.PrescriptionRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.Instant
import javax.inject.Inject

class PrescriptionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : PrescriptionRepository {

    override suspend fun getPrescriptionByAppointment(appointmentId: String): Prescription? {
        Log.d(TAG, "[Supabase] getPrescriptionByAppointment: appointmentId=$appointmentId")
        return try {
            val list = postgrest["prescriptions"]
                .select {
                    filter {
                        eq("appointment_id", appointmentId)
                    }
                }
                .decodeList<Prescription>()
            val result = list.firstOrNull()
            Log.d(TAG, "[Supabase] getPrescriptionByAppointment success: found=${result != null}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getPrescriptionByAppointment SELECT FAILED for appointmentId=$appointmentId", e)
            e.printStackTrace()
            null
        }
    }

    override suspend fun sendPrescriptionToPharmacy(prescriptionId: String, pharmacyId: String): Boolean {
        Log.d(TAG, "[Supabase] sendPrescriptionToPharmacy: prescriptionId=$prescriptionId, pharmacyId=$pharmacyId")
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
            Log.d(TAG, "[Supabase] sendPrescriptionToPharmacy success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] sendPrescriptionToPharmacy UPDATE FAILED for prescriptionId=$prescriptionId", e)
            e.printStackTrace()
            false
        }
    }

    companion object {
        private const val TAG = "PrescriptionRepo"
    }
}
