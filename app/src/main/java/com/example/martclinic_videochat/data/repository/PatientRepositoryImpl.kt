package com.example.martclinic_videochat.data.repository

import android.util.Log
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.repository.PatientRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class PatientRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : PatientRepository {

    override suspend fun getPatients(): List<Patient> {
        val userId = auth.currentUserOrNull()?.id ?: return emptyList()
        Log.d(TAG, "[Supabase] getPatients: user_id=$userId")
        return try {
            val result = postgrest["patients"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Patient>()
            Log.d(TAG, "[Supabase] getPatients success: ${result.size} patients")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getPatients SELECT FAILED for user_id=$userId", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getAllPatients(): List<Patient> {
        Log.d(TAG, "[Supabase] getAllPatients")
        return try {
            val result = postgrest["patients"]
                .select {
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Patient>()
            Log.d(TAG, "[Supabase] getAllPatients success: ${result.size} patients")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getAllPatients SELECT FAILED", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFirstPatient(): Patient? {
        val userId = auth.currentUserOrNull()?.id ?: return null
        Log.d(TAG, "[Supabase] getFirstPatient: user_id=$userId")
        return try {
            val list = postgrest["patients"]
                .select {
                    filter { 
                        eq("user_id", userId)
                        eq("relationship", "본인") 
                    }
                }
                .decodeList<Patient>()
            val result = list.firstOrNull()
            Log.d(TAG, "[Supabase] getFirstPatient success: patient=${result?.name}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getFirstPatient SELECT FAILED for user_id=$userId", e)
            e.printStackTrace()
            null
        }
    }

    override suspend fun createPatient(patient: Patient): Boolean {
        Log.d(TAG, "[Supabase] createPatient: name=${patient.name}, relationship=${patient.relationship}")
        return try {
            postgrest["patients"].insert(patient)
            Log.d(TAG, "[Supabase] createPatient success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] createPatient INSERT FAILED for name=${patient.name}", e)
            throw e
        }
    }

    override suspend fun updatePatient(patient: Patient): Boolean {
        if (patient.id == null) return false
        Log.d(TAG, "[Supabase] updatePatient: id=${patient.id}, name=${patient.name}")
        return try {
            postgrest["patients"].update(patient) {
                filter { eq("id", patient.id) }
            }
            Log.d(TAG, "[Supabase] updatePatient success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] updatePatient UPDATE FAILED for id=${patient.id}", e)
            e.printStackTrace()
            false
        }
    }

    override suspend fun deletePatient(patientId: String): Boolean {
        Log.d(TAG, "[Supabase] deletePatient: patientId=$patientId")
        return try {
            postgrest["patients"].delete {
                filter { eq("id", patientId) }
            }
            Log.d(TAG, "[Supabase] deletePatient success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] deletePatient DELETE FAILED for patientId=$patientId", e)
            e.printStackTrace()
            false
        }
    }

    companion object {
        private const val TAG = "PatientRepo"
    }
}
