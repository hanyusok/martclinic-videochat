package com.example.martclinic_videochat.domain.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String? = null,
    val patient_id: String,
    val schedule_id: String? = null, // Optional in ASAP mode
    val status: String,
    val symptoms: String,
    val queue_number: Int? = null,
    val estimated_wait_minutes: Int? = null,
    val meet_link: String? = null,
    val payment_amount: Int? = null,
    val created_at: String? = null
) {
    companion object {
        val ACTIVE_STATUSES = listOf("waiting", "calling", "in_progress")
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CALLING = "calling"
        const val STATUS_WAITING = "waiting"
        const val STATUS_IN_PROGRESS = "in_progress"
    }

    val statusText: String
        get() = when (status) {
            "waiting" -> "대기 중"
            "calling" -> "진료 입장 대기"
            "in_progress" -> "진료 중"
            "completed" -> "진료 완료"
            "cancelled" -> "진료 취소"
            else -> status
        }

    @Composable
    fun getStatusColor(): Color {
        return when (status) {
            "waiting" -> MaterialTheme.colorScheme.tertiary
            "calling" -> MaterialTheme.colorScheme.secondary
            "in_progress" -> MaterialTheme.colorScheme.primary
            "completed" -> MaterialTheme.colorScheme.outline
            "cancelled" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    }
}
