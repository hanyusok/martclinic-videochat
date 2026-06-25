package com.example.martclinic_videochat.data.repository

import android.util.Log
import com.example.martclinic_videochat.domain.model.Payment
import com.example.martclinic_videochat.domain.repository.PaymentRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : PaymentRepository {

    override suspend fun createPayment(payment: Payment) {
        Log.d(TAG, "[Supabase] createPayment: appointment_id=${payment.appointment_id}, transaction_id=${payment.transaction_id}")
        try {
            postgrest["payments"].insert(payment)
            Log.d(TAG, "[Supabase] createPayment success")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] createPayment INSERT FAILED", e)
            throw e
        }
    }

    override suspend fun getPaymentsForAppointment(appointmentId: String): List<Payment> {
        Log.d(TAG, "[Supabase] getPaymentsForAppointment: appointment_id=$appointmentId")
        return try {
            val result = postgrest["payments"]
                .select { filter { eq("appointment_id", appointmentId) } }
                .decodeList<Payment>()
            Log.d(TAG, "[Supabase] getPaymentsForAppointment success: ${result.size} rows")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] getPaymentsForAppointment FAILED", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updatePaymentStatus(appointmentId: String, status: String) {
        Log.d(TAG, "[Supabase] updatePaymentStatus: appointmentId=$appointmentId, status=$status")
        try {
            postgrest["payments"].update(mapOf("status" to status)) {
                filter { eq("appointment_id", appointmentId) }
            }
            Log.d(TAG, "[Supabase] updatePaymentStatus success")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase] updatePaymentStatus UPDATE FAILED for appointmentId=$appointmentId", e)
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "PaymentRepo"
    }
}
