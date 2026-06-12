package com.example.martclinic_videochat.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.presentation.viewmodel.HistoryViewModel
import com.example.martclinic_videochat.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val patient by viewModel.patient.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("진료 기록", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadHistory() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (appointments.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "비어 있음",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "과거 진료 내역이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(appointments) { appointment ->
                        HistoryItem(appointment = appointment)
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun HistoryItem(appointment: Appointment) {
    val formattedTime = DateTimeUtil.formatTimestampToKst(appointment.created_at)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusText = when (appointment.status) {
                    "pending" -> "접수 대기"
                    "paid" -> "결제 완료"
                    "confirmed" -> "예약 확정"
                    "in_progress" -> "진료 중"
                    "completed" -> "진료 완료"
                    "cancelled" -> "예약 취소"
                    else -> appointment.status
                }
                
                val statusColor = when (appointment.status) {
                    "completed" -> MaterialTheme.colorScheme.primary
                    "cancelled" -> MaterialTheme.colorScheme.error
                    "in_progress" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                }

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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "증상:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = appointment.symptoms,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (formattedTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "접수 일시: $formattedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Entry Button for ongoing consults
            if (appointment.status in listOf("confirmed", "in_progress") && !appointment.meet_link.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Launch Meet action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("비대면 영상 진료 입장", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
