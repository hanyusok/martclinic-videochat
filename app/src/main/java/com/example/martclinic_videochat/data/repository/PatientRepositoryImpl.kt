package com.example.martclinic_videochat.data.repository

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
        return try {
            postgrest["patients"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Patient>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFirstPatient(): Patient? {
        val userId = auth.currentUserOrNull()?.id ?: return null
        return try {
            val list = postgrest["patients"]
                .select {
                    filter { 
                        eq("user_id", userId)
                        eq("relationship", "본인") 
                    }
                }
                .decodeList<Patient>()
            list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createPatient(patient: Patient): Boolean {
        return try {
            postgrest["patients"].insert(patient)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updatePatient(patient: Patient): Boolean {
        if (patient.id == null) return false
        return try {
            postgrest["patients"].update(patient) {
                filter { eq("id", patient.id) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deletePatient(patientId: String): Boolean {
        return try {
            postgrest["patients"].delete {
                filter { eq("id", patientId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
