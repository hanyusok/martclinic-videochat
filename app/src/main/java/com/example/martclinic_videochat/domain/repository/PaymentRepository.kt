package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Payment

interface PaymentRepository {
    suspend fun createPayment(payment: Payment)
    suspend fun getPaymentsForAppointment(appointmentId: String): List<Payment>
    suspend fun updatePaymentStatus(appointmentId: String, status: String)
}
