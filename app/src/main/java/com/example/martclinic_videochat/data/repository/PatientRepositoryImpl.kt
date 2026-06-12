package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.repository.PatientRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class PatientRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth // Inject Auth
) : PatientRepository {

    override suspend fun getFirstPatient(): Patient? {
        val userId = auth.currentUserOrNull()?.id ?: return null
        return try {
            val list = postgrest["patients"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Patient>()
            list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getPatientById(id: String): Patient? {
        return try {
            val list = postgrest["patients"]
                .select {
                    filter {
                        eq("id", id)
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
}
