package com.example.martclinic_videochat.domain.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String? = null,
    val patient_id: String,
    val schedule_id: String,
    val status: String,
    val symptoms: String,
    val meet_link: String? = null,
    val payment_amount: Int? = null,
    val created_at: String? = null
) {
    val statusText: String
        get() = when (status) {
            "pending" -> "접수 대기"
            "paid" -> "결제 완료"
            "confirmed" -> "예약 확정"
            "in_progress" -> "진료 중"
            "completed" -> "진료 완료"
            "cancelled" -> "예약 취소"
            else -> status
        }

    @Composable
    fun getStatusColor(): Color {
        return when (status) {
            "completed" -> MaterialTheme.colorScheme.primary
            "cancelled" -> MaterialTheme.colorScheme.error
            "in_progress" -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.tertiary
        }
    }
}