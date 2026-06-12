package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.repository.PatientRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class PatientRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : PatientRepository {

    override suspend fun getFirstPatient(): Patient? {
        return try {
            val list = postgrest["patients"]
                .select {
                    limit(1)
                    order("created_at", Order.ASCENDING)
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
}
