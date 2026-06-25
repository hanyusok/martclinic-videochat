package com.example.martclinic_videochat.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Payment
import com.example.martclinic_videochat.util.DateTimeUtil

@Composable
fun AppointmentCard(
    appointment: Appointment,
    payment: Payment? = null,
    patientName: String? = null,
    onEnterConsultation: (() -> Unit)? = null,
    onViewPrescription: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusText = appointment.statusText
                val statusColor = appointment.getStatusColor()

                SuggestionChip(
                    onClick = {},
                    label = { Text(statusText, fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )

                appointment.payment_amount?.let { amount ->
                    Text(
                        text = "${String.format("%,d", amount)}원",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "증상:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            val symptomText = if (patientName != null) "[$patientName] ${appointment.symptoms}" else appointment.symptoms
            Text(
                text = symptomText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )

            appointment.created_at?.let { createdAt ->
                val formattedTime = DateTimeUtil.formatTimestampToKst(createdAt)
                if (formattedTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "접수 일시: $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (payment != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("결제 내역", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                val tidText = payment.transaction_id.let { if (it == "OFFLINE_PAYMENT") "현장 결제" else it }
                Text("승인 번호: ${tidText ?: "없음"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (payment.pay_method != null) {
                    Text("결제 수단: ${payment.pay_method}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Entry Button for ongoing consults
            if (appointment.status in listOf("confirmed", "waiting", "calling", Appointment.STATUS_IN_PROGRESS) && !appointment.meet_link.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onEnterConsultation?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("비대면 영상 진료 입장", fontWeight = FontWeight.Bold)
                }
            }

            // Prescription View Button for completed consults
            if (appointment.status == Appointment.STATUS_COMPLETED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onViewPrescription?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("전자 처방전 조회", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
